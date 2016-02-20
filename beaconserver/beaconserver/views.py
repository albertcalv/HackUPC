from django.http import HttpResponse
from django.shortcuts import render

import json


def load_dummy_data():
    matrix = [[0, 10, 3, 12, 23, 34],[10, 0, 3, 12, 23, 34],[3, 10, 0, 12, 23, 34], [12, 10, 3, 0, 23, 34], [23, 10, 3, 12, 0, 34], [34, 10, 3, 12, 23, 0]]
    
    user = [60,34,80]
    
    
def get_dummy():
    return 111

def make_json(list_sites):
    response_data = {}
    it = 0
    for site in list_sites: 
        id  = site[0]
        dsc = site[1]        
        name = "ID"   + str(it)
        dsc  = "DESC" + str(it)        
        response_data[name] = id
        response_data[dsc]  = dsc        
        it += 1
        
    return response_data
    


def index(request):
    
    
    load_dummy_data()
       
    if request.GET.get('date_ini') and request.GET.get('date_end'):
        date_ini = request.GET['date_ini']
        date_end = request.GET['date_end']
    if request.GET.get('money'):
        money = request.GET['money']
    else: 
        money = 0
    if request.GET.get('topic'):
        topic = request.GET['topic']
    else: 
        topic = 0

    vv = get_dummy()
    
    
    list_sites = []
    
    site_1 = ['000', 'description0']
    list_sites.append(site_1)
    
    site_2 = ['001', 'description1']
    list_sites.append(site_2)
    
    site_3 = ['002', 'description2']
    list_sites.append(site_3)
    
        
    response_data = make_json(list_sites)
    
    


    message = money
    return HttpResponse(json.dumps(response_data), content_type="application/json")


