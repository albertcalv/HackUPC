"""hackUPC URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/1.8/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  url(r'^$', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  url(r'^$', Home.as_view(), name='home')
Including another URLconf
    1. Add a URL to urlpatterns:  url(r'^blog/', include('blog.urls'))
"""
from django.conf.urls import include, url
from django.contrib import admin

from django.http import HttpResponse
from django.shortcuts import render

import json

beacons_data = {}
with open('../beacons/places.json', 'r') as beacons_file:
    beacons_data = json.loads(beacons_file.read().replace('\n', ''))
beacons = [b for b in beacons_data['beacons']]
beacons_by_id = {};
for beacon in beacons:
    beacons_by_id[beacon['id']] = beacon
    
def beacon_info(request):
    id = request.GET.get('id')
    return HttpResponse(json.dumps(beacons_by_id[id]), content_type="application/json")
    
    
urlpatterns = [
    url(r'^admin/', include(admin.site.urls)),
    url(r'^beaconserver/', include('beaconserver.urls')),
    url(r'^beaconinfo/', beacon_info),
]
