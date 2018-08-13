# TankControl
Application source code for a tank control system application

Cal Poly Pomona Decentralized Water Treatment Project

The purpose of this application is to monitor and control an arduino based control system using Internet of Things (IOT) technology.

Breakdown of classes:
   Main Activity - Houses motor and valve control buttons as well as temperature, conductivity, flow rate, pressure, capacity used,
                   and power usage
   More Details - displays additional data on the water tanks and the voltage and current
   MySingleton - handles http requests to ThingSpeak. Data is sent to this queue from the main activity and more details activity
   Welcome_Screen - Opening screen, no explanation needed
