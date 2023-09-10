# Read me

This repository contains the solution to the first phase of the TU Wien course [183.241 Software Engineering and Project Management (2023SS)](https://tiss.tuwien.ac.at/course/educationDetails.xhtml?dswid=1107&dsrid=680&semester=2023S&courseNr=183241).

## Grading

    * Gesamtpunkte: 71
    ** Userstories (erreicht): 80
    ** Techstories (Abzüge): -9
    *** Qualitätsmanager/in: -1
    *** Usability Engineer: -1
    *** Technischer Architekt/in: -7
    *** Datenmanager/in: 0

    [TS11] -1
    Im OwnerService interface fehlt bei der create Methode JavaDoc für die ConflictException

    [TS19] -1
    Es ist nicht ersichtlich, dass das Datumsfeld das heutige Datum annimmt, wenn der Benutzer nichts eingibt.

    [TS25] -4
    Auf der Startseite/Listenseite wird beim initialen Laden zwei Mal ein Request mit der ganzen Pferdeliste geschickt. Einer von diesen ist unnötig und verursacht nur unnötig Traffic!

    [TS26] -3
    Bei der Methode HorseServiceImpl.horseMapForIds wird eine NotFoundException gewrappt, aber nicht an die neue FatalException weitergegeben! Somit geht wichtige Debug-Info verloren!

Final Points: 71 / 80
