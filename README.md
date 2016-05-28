Adobe-Address project is used to verify address using Google Maps API.
The addresses can be either in a sheet in Google public cloud or in a local csv file. 
The first row in the file must be field names, which are Address, City, State_Province, Postal_Code, Country, Google Verified Address, Latitude and Longitude.

** To build the war file in order to deploy it on any webserver
    mvn clean package


** To build a fat jar with all dependencies
    mvn clean compile assembly:single -P app


** To run the jar file
    java -jar address-verify.jar
