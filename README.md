# topical-backend
An API on top of an Exchange server (version 2010_SP2) that:
* lists the configured meeting rooms 
* lists the apppointments for a meeting room 
* lets you add a new appointment to a meeting room

## Usage

* `/rooms` returns a list of all the rooms
* `/events/:room` returns a list of the meetings scheduled for the room within the next 7 days, starting at midnight last night
* `/claim/:room` claims the room for the next hour or until the next scheduled meeting, whichever comes first

## Config
See the static Strings in TopicalBackend.java
