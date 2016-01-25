# topical-backend
An API on top of an Exchange server (version 2010_SP2) that:
* lists the configured meeting rooms 
* lists the apppointments for a meeting room 
* lets you add a new appointment to a meeting room

## API

* `/rooms` returns a list of all the rooms
* `/events/:room` returns a list of the meetings scheduled for today
* `/claim/:room` claims the room for the next hour or until the next scheduled meeting, whichever comes first

## Usage / config
Configuration is done by passing the following arguments to the program:

`[domain] [username] [password] [url] [room1] [room2] [roomX]`
