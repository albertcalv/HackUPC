from django.http import HttpResponse
from django.shortcuts import render

import json
import time
import datetime

# =========
# ALGORITHM
# =========
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
        self.features = beacon['features']
        # Add more things here...

        Beacon.beacon_count = Beacon.beacon_count + 1

beacons = [Beacon(b) for b in beacons_data['beacons']]
time_matrix = beacons_data['time_matrix']

beacons_by_id = {};
for beacon in beacons:
    beacons_by_id[beacon.id] = beacon

beacon_json = [b for b in beacons_data['beacons']]
beacon_json_by_id = {};
for b in beacon_json:
    beacon_json_by_id[b['id']] = b
    
beacon_ids = [b.id for b in beacons]

def imm_append(lst, new):
    return [new] + [x for x in lst]

def path_time(path):
    if len(path) <= 1: return time_matrix[beacons_by_id[path[0]].index][beacons_by_id["0000"].index]

    bi = path[0]
    bj = path[1]
    tail = path[1:]
    
    this_cost = beacons_by_id[bi].stay_time + time_matrix[beacons_by_id[bi].index][beacons_by_id[bj].index];
    return this_cost + path_time(tail)

def jaccard(s1, s2):
    return len(set(s1).intersection(s2)) / len(set(s1).union(set(s2)))

def preference_score(path, preferences):
    beacon = beacons_by_id[path[0]]
    
    score = jaccard(beacon.features, preferences) 

    if len(path) > 1:
        return preference_score(path[1:], preferences)
    else:
        return score

# state [b1, b3, b4 ...]
class BeaconProblem():
    def __init__(self, initial_state, max_time, max_price, preferences):
        self.max_time = max_time
        self.max_price = max_price
        self.initial_state = initial_state
        self.preferences = preferences

    def successor(self, state):
        return [imm_append(state, x) for x in beacon_ids if x not in state]
        
    def value(self, state):
        money_cost = sum([beacons_by_id[x].price for x in state])
        time_cost = path_time(state)
        pref_score = preference_score(state, self.preferences)
        
        if money_cost > self.max_price or time_cost > self.max_time: return -100000000 
        else: return preference_score
    
    def initial(self):
        return self.initial_state

def format_answer(path):
    path.reverse()
    answer = []
    for i in range(0, len(path) - 1):
        answer.append(path[i])
        t = time_matrix[beacons_by_id[path[i]].index][beacons_by_id[path[i+1]].index]
	answer.append(t)
    answer.append(path[len(path)-1])
    t = time_matrix[beacons_by_id[path[len(path)-1]].index][beacons_by_id['0000'].index]
    answer.append(t)
    return answer
    
def solve(problem):
    current = problem.initial()
    current_val = 0
    while True:
        max_val = -100000000 
        max_val_idx = -1
        successors = problem.successor(current)
        for i in range(0, len(successors)):
            v = problem.value(successors[i])
            if v > max_val:
                max_val = v
                max_val_idx = i
            
        if max_val < current_val or max_val_idx == -1:
            return format_answer(current)
        current = successors[max_val_idx]

def index(request):
    if request.GET.get('date_ini') and request.GET.get('date_end'):
        date_ini = request.GET['date_ini']
        date_end = request.GET['date_end']

    start_time = time.mktime(datetime.datetime.strptime(date_ini, "%d/%m/%Y %H:%M").timetuple())
    end_time = time.mktime(datetime.datetime.strptime(date_end, "%d/%m/%Y %H:%M").timetuple())

    available_time = (end_time - start_time) / 60

    if request.GET.get('money'):
        money = int(request.GET['money'])
    else: 
        money = 0

    import ast

    if request.GET.get('preferences'):
        preferences = ast.literal_eval(request.GET.get('preferences'))

    p = BeaconProblem(['0000'], available_time, money, preferences)
    list_sites = list(solve(p))
    
    response_data = ({"beacons": list_sites, "beacons_by_id": beacon_json_by_id})
    
    return HttpResponse(json.dumps(response_data), content_type="application/json")
