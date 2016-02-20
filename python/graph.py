#!/bin/python
import json
import datetime
import search

start_beacon = "7bDw"

beacons_data = {}
with open('../beacons/places.json', 'r') as beacons_file:
    beacons_data = json.loads(beacons_file.read().replace('\n', ''))

class Beacon:
    """A beacon"""
    beacon_count = 0

    def __init__(self, beacon):
        self.id = beacon['id']
        self.index = Beacon.beacon_count
        self.price = beacon['price']
        self.stay_time = beacon['stay_time']
        self.preferences = beacon['preferences']
        # Add more things here...

        Beacon.beacon_count = Beacon.beacon_count + 1

beacons = [Beacon(b) for b in beacons_data['beacons']]
time_matrix = beacons_data['time_matrix']


beacons_by_id = {};
for beacon in beacons:
    beacons_by_id[beacon.id] = beacon
beacon_ids = [b.id for b in beacons]

def imm_append(lst, new):
    return [new] + [x for x in lst]

def path_time(path):
    if len(path) <= 1: return 0

    bi = path[0]
    bj = path[1]
    tail = path[1:]
    
    this_cost = time_matrix[beacons_by_id[bi].index][beacons_by_id[bj].index];
    return this_cost + path_time(tail)

# state [b1, b3, b4 ...]
class BeaconProblem():
    def __init__(self, initial_stat, max_time, max_price, goal=None):
        self.max_time = max_time
        self.max_price = max_price
        self.initial_state = initial_state

    def successor(self, state):
        return [imm_append(state, x) for x in beacon_ids if x not in state]
        
    def value(self, state):
        money_cost = sum([beacons_by_id[x].price for x in state])
        time_cost = path_time(state)
        
        if money_cost > self.max_price or time_cost > self.max_time: return -100000000 
        else: return money_cost + time_cost
    
    def initial(self):
        return self.initial_state


p = BeaconProblem(['GqUQ'], 100, 60)
def solve(problem):
    current = problem.initial()
    current_val = 0
    while True:
        max_val = -100000000 
        max_val_idx = -1
        successors = p.successor(current)
        for i in range(0, len(successors)):
            v = p.value(successors[i])
            if v > max_val:
                max_val = v
                max_val_idx = i
            
        if max_val < current_val or max_val_idx == -1:
            return current
        current = successors[max_val_idx]

print(hill_climbing(p))