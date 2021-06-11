# Hospital Database Project CS166
Group 22: Jourdon Freeman & Ivan Carrillo \
Odd Problems (1, 3, 5, 7): Jourdon Freeman \
Even Problems (2, 4, 6, 8): Ivan Carrillo \

## Problem 1

## Problem 2 - Add Patient

The add patient function takes in a name, gender, age and address. \
![Normal input](https://github.com/IvanBot00/Hospital_DB_Project_CS166/blob/main/images/q2normal.png)

Error checking is done for the gender and age fields. \
![Input with errors](https://github.com/IvanBot00/Hospital_DB_Project_CS166/blob/main/images/q2error.png)

The age input must be between 0 and 150.

## Problem 3

## Problem 4 - Make Appointment

The make appointment function lets the user make an appointment from appointmens which are available ('AV'). The appointment is then marked as active ('AC').

![Normal input](https://github.com/IvanBot00/Hospital_DB_Project_CS166/blob/main/images/q4normal1.png)
![Normal input 2](https://github.com/IvanBot00/Hospital_DB_Project_CS166/blob/main/images/q4normal2.png)

### No doctors available
Error checking is done when accepting the inputs for names, ids, and appointment numbers. When there are no appointments for the doctors of a specific department, the program will return to the menu.

![No Doctor](https://github.com/IvanBot00/Hospital_DB_Project_CS166/blob/main/images/q4nodoc.png)

### No appointments available

The program also returns to the main menu when the chosen doctor has no available appointments.

![No appointment available](https://github.com/IvanBot00/Hospital_DB_Project_CS166/blob/main/images/q4noapp.png)

## Problem 5

## Problem 6 - List Available Appointments

The program lists all the appointments available for a given doctor.

![List available appointments](https://github.com/IvanBot00/Hospital_DB_Project_CS166/blob/main/images/q6.png)



## Problem 7

## Problem 8

The program lists the number of appointments a doctor has with a given status. 

![List number of appointments with status](https://github.com/IvanBot00/Hospital_DB_Project_CS166/blob/main/images/q8enterPA.png)

The problem checks for a correct input which is one of the four listed.

![Incorrect Input](https://github.com/IvanBot00/Hospital_DB_Project_CS166/blob/main/images/q8error.png)
