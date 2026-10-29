#!/usr/bin/env python3
"""Host-only regression checks for the release verifier's asset exceptions."""
from pathlib import Path
import runpy
import unittest

verify=runpy.run_path(str(Path(__file__).with_name('verify-release.py')))
compare=verify['compare_packaged_assets']
IDENTITY=verify['IDENTITY_ASSET']
PROFILES=verify['DEXOPT_ASSETS']

class AssetPolicyTests(unittest.TestCase):
    def setUp(self):
        self.lab={f'assets/art/v118/companion-{i:03d}-poses.{suffix}':'a'*64
                  for i in range(148) for suffix in ('webp','json')}
        self.lab.update({name:'b'*64 for name in PROFILES})
        self.lab['assets/LICENSE.txt']='c'*64
        self.production=dict(self.lab)
        self.production[IDENTITY]='d'*64
        for name in PROFILES:self.production[name]='e'*64

    def test_only_named_variant_differences_pass(self):
        report=compare(self.production,self.lab)
        self.assertEqual(report['productionOnly'],[IDENTITY])
        self.assertEqual(report['commonDifferent'],sorted(PROFILES))
        self.assertEqual(report['v118ArtFiles'],296)
        self.assertTrue(report['v118ArtEqual'])

    def test_identical_compiled_profiles_also_pass(self):
        for name in PROFILES:self.production[name]=self.lab[name]
        self.assertEqual(compare(self.production,self.lab)['commonDifferent'],[])

    def test_unexpected_asset_paths_are_rejected(self):
        for variant,path in [('production','assets/unexpected.json'),
                             ('lab','assets/unexpected.json'),('lab',IDENTITY)]:
            with self.subTest(variant=variant,path=path):
                production,lab=dict(self.production),dict(self.lab)
                (production if variant=='production' else lab)[path]='f'*64
                with self.assertRaises(AssertionError):compare(production,lab)

    def test_every_other_common_byte_difference_is_rejected(self):
        for path in ['assets/art/v118/companion-000-poses.webp',
                     'assets/art/v118/companion-000-poses.json','assets/LICENSE.txt']:
            with self.subTest(path=path):
                production=dict(self.production);production[path]='f'*64
                with self.assertRaises(AssertionError):compare(production,self.lab)

    def test_missing_required_files_are_rejected(self):
        cases=[('production',IDENTITY),('both','assets/art/v118/companion-000-poses.webp'),
               ('production','assets/art/v118/companion-000-poses.json'),
               ('both','assets/dexopt/baseline.prof')]
        for variant,path in cases:
            with self.subTest(variant=variant,path=path):
                production,lab=dict(self.production),dict(self.lab)
                if variant in ('production','both'):del production[path]
                if variant=='both':del lab[path]
                with self.assertRaises(AssertionError):compare(production,lab)

if __name__=='__main__':unittest.main(verbosity=2)
