#!/usr/bin/env python3
"""Public-origin verification is unavailable under the recorded browser permission boundary.

Do not fetch or render the origin through another tool as a workaround. Local
browser and authenticated Hosting-management receipts remain separate evidence.
"""
import argparse

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.parse_args()
    parser.exit(2,"No public-origin request made. Origin access was refused during 1.19 qualification; this tool does not bypass that boundary. Use local rendering and Hosting management evidence, and report public rendering as unverified.\n")

if __name__=='__main__':main()
