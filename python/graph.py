#!/bin/python
import json

beacons_data = {}
with open('beacons/places.json', 'r') as beacons_file:
    beacons_data = json.loads(beacons_file.read().replace('\n', ''))

class Beacon:
    """A beacon"""
    def __init__(self, beacon):
        self.id = beacon['id']
        # Add more things here...

beacons = [Beacon(b) for b in beacons_data['beacons']]


