#!/usr/bin/env python3
"""Create the separately authorized empty final-H device; preserve E/G profiles."""
from pathlib import Path
import importlib.util

path=Path(__file__).with_name('prepare-device.py')
spec=importlib.util.spec_from_file_location('fresh_device_creation',path)
device=importlib.util.module_from_spec(spec)
spec.loader.exec_module(device)
device.NAME='Luma118-Final-H-QA'
device.PORT=5600
device.SERIAL='emulator-5600'
device.OUT=device.ROOT/'docs/validation/v1.18/native-fresh-h/device-creation'

if __name__=='__main__':device.main()
