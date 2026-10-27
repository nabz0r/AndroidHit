"""Owned guest QA infrastructure fault; never changes preference contents."""
from pathlib import Path
import json
import os
import subprocess
import tempfile
import threading


class StorageFaultController:
    def __init__(self, adb, serial, prefdir, remote, out):
        if serial not in ['emulator-5580', 'emulator-5590'] or prefdir != '/data/user/0/com.androidhit.lumadrift.sandbox/shared_prefs':
            raise AssertionError('Storage fault is restricted to the owned guest Lab QA target')
        self.adb, self.serial, self.prefdir, self.remote, self.out = adb, serial, prefdir, remote, out
        self.original_mode = self._shell('su', '0', 'stat', '-c', '%a', prefdir).strip()
        if not self.original_mode.isdigit():
            raise AssertionError('Cannot identify original preference-directory permissions')
        self.stop = threading.Event()
        self.events = []
        self.error = None
        self.last_request = None
        self.locked = False
        self.restored = False
        self.lock_count = 0
        self.restore_count = 0
        self.sequence = ['guardian3-session-lock', 'guardian3-session-restore', 'guardian3-reward-lock', 'guardian3-reward-restore']
        self.thread = threading.Thread(target=self._loop, name='luma-qa-storage-fault', daemon=True)

    def _cmd(self, *parts):
        return subprocess.check_output([self.adb, '-s', self.serial, *parts], stderr=subprocess.STDOUT, timeout=10)

    def _shell(self, *parts):
        return self._cmd('shell', *parts).decode(errors='replace')

    def start(self):
        self.thread.start()

    def _acknowledge(self, request, mode):
        value = {'request': request['request'], 'stage': request['stage'], 'ok': True, 'mode': mode}
        with tempfile.TemporaryDirectory(prefix='luma-v19-storage-fault-ack-') as temp:
            file = Path(temp) / 'ack.json'
            file.write_text(json.dumps(value))
            staging = '/data/local/tmp/luma-v19-fault-ack-' + str(os.getpid())
            self._cmd('push', str(file), staging)
            try:
                self._shell('su', '0', 'cp', staging, self.remote + '/storage-fault-ack.tmp')
                self._shell('su', '0', 'mv', self.remote + '/storage-fault-ack.tmp', self.remote + '/storage-fault-ack.json')
            finally:
                self._shell('rm', '-f', staging)

    def _loop(self):
        try:
            while not self.stop.is_set():
                read = subprocess.run([self.adb, '-s', self.serial, 'exec-out', 'cat', self.remote + '/storage-fault-request.json'], capture_output=True, timeout=10)
                if read.returncode == 0:
                    try:
                        request = json.loads(read.stdout)
                    except (ValueError, UnicodeDecodeError):
                        request = None
                    if request and request.get('request') != self.last_request:
                        stage = request.get('stage')
                        if request.get('request') not in self.sequence or stage not in ['lock', 'restore']:
                            raise AssertionError('Unknown storage-fault handshake')
                        if len(self.events) >= len(self.sequence) or request['request'] != self.sequence[len(self.events)]:
                            raise AssertionError('Storage fault phases must follow the fixed session/reward sequence')
                        if stage == 'lock':
                            if self.lock_count != self.restore_count:
                                raise AssertionError('Previous permission lock must first be restored')
                            self._shell('su', '0', 'chmod', '500', self.prefdir)
                            self.locked = True
                            self.lock_count += 1
                            expected = '500'
                        else:
                            if self.lock_count != self.restore_count + 1:
                                raise AssertionError('Each permission restore must follow a lock')
                            self._shell('su', '0', 'chmod', self.original_mode, self.prefdir)
                            expected = self.original_mode
                            self.restored = True
                            self.restore_count += 1
                        observed = self._shell('su', '0', 'stat', '-c', '%a', self.prefdir).strip()
                        if observed != expected:
                            raise AssertionError('Preference-directory permission change was not observed')
                        self.events.append({'request': request['request'], 'stage': stage, 'observed_mode': observed})
                        self.last_request = request['request']
                        self._acknowledge(request, observed)
                self.stop.wait(0.5)
        except BaseException as failure:
            self.error = str(failure)

    def close(self):
        self.stop.set()
        self.thread.join(timeout=15)
        failure = None
        try:
            self._shell('su', '0', 'chmod', self.original_mode, self.prefdir)
            observed = self._shell('su', '0', 'stat', '-c', '%a', self.prefdir).strip()
            if observed != self.original_mode:
                raise AssertionError('Original preference-directory permissions were not restored')
            if self.thread.is_alive():
                raise AssertionError('Storage-fault controller did not stop')
            if self.error:
                raise AssertionError(self.error)
        except BaseException as error:
            failure = error
        report = {'scope': 'Temporary filesystem permission fault on the explicitly owned guest Lab QA emulator; no preference values injected or edited', 'serial': self.serial, 'directory': self.prefdir, 'original_mode': self.original_mode, 'final_mode': locals().get('observed'), 'permissions_restored': locals().get('observed') == self.original_mode, 'requested_lock_observed': self.locked, 'requested_restore_observed': self.restored, 'observed_lock_count': self.lock_count, 'observed_restore_count': self.restore_count, 'events': self.events, 'controller_error': self.error, 'cleanup_error': str(failure) if failure else None}
        (self.out / 'storage-fault-infrastructure.json').write_text(json.dumps(report, indent=2) + '\n')
        if failure:
            raise failure
        return report
