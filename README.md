The adobe-address project is used to verfiy address by Google Maps API.

The address data can be either in an excel file in Google pucli cloud or in local csv file.
The first row in the file must be field names, which are Address, City, State_Province, Postal_Code, Country, Google Verified Address, Latitude and Longitude. 

** To build the war for deployment
	mvn clean package

** To build the jar file for verifying Google sheet
	mvn clean compile assembly:single -P app

