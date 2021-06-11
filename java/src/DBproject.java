/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class DBproject{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + DBproject.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		DBproject esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new DBproject (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Doctor");
				System.out.println("2. Add Patient");
				System.out.println("3. Add Appointment");
				System.out.println("4. Make an Appointment");
				System.out.println("5. List appointments of a given doctor");
				System.out.println("6. List all available appointments of a given department");
				System.out.println("7. List total number of different types of appointments per doctor in descending order");
				System.out.println("8. Find total number of patients per doctor with a given status");
				System.out.println("9. < EXIT");
				
				switch (readChoice()){
					case 1: AddDoctor(esql); break;
					case 2: AddPatient(esql); break;
					case 3: AddAppointment(esql); break;
					case 4: MakeAppointment(esql); break;
					case 5: ListAppointmentsOfDoctor(esql); break;
					case 6: ListAvailableAppointmentsOfDepartment(esql); break;
					case 7: ListStatusNumberOfAppointmentsPerDoctor(esql); break;
					case 8: FindPatientsCountWithStatus(esql); break;
					case 9: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	public static void AddDoctor(DBproject esql) {//1
		int docID;
		String dname;
		String special;
		int depID;
		String query;
		String hospitalName;
		int hosID;
		String departmentName;
		List<List<String>> result;
		List<String> record;

		//Create doctorID
		do {
			try {
				query = "SELECT COUNT(doctor_ID) FROM Doctor";
				record = esql.executeQueryAndReturnResult(query).get(0);
				docID = Integer.parseInt(record.get(0));
				docID++;
				break;
			}catch (Exception e) {
				System.out.println(e.getMessage());
				continue;
			}
		}while (true);

		//Get Doctor name
		do {
			System.out.print("Please enter the doctor's name: ");
			try {
				dname = in.readLine();
				break;
			}catch (Exception e) {
				System.out.println("The doctor's name must be a string.");
				continue;
			}
		}while (true);

		//Get specialty
		System.out.println("========== Specialties ==========");
		try {
			query = "SELECT DISTINCT specialty FROM Doctor";
			esql.executeQueryAndPrintResult(query);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		System.out.println("===================================");

		do {
			try {
				System.out.print("Please enter a specialty: ");
				special = in.readLine();

				query = "SELECT * FROM Doctor WHERE LOWER(specialty) LIKE LOWER('%" + special + "%')";
				result = esql.executeQueryAndReturnResult(query);

				if (result.isEmpty()) {
					System.out.println("Specialty not listed.");
					continue;
				}

				record = result.get(0);
				System.out.println("Selected Record: " + record);

				break;
			}catch(Exception e) {
				System.out.println("Invalid input." + e.getMessage());
				continue;
			}
		}while(true);

		//Get hospital name
		System.out.println("========== Hospital List ==========");
		try {
			query = "SELECT name FROM Hospital";
			esql.executeQueryAndPrintResult(query);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		System.out.println("===================================");

		do {
			try {
				System.out.print("Please enter the hospital name: ");
				hospitalName = in.readLine();

				query = "SELECT * FROM Hospital WHERE LOWER(name) LIKE LOWER('%" + hospitalName + "%')";
				result = esql.executeQueryAndReturnResult(query);

				if (result.isEmpty()) {
					System.out.println("Did not find a hospital with that name.");
					continue;
				}

				record = result.get(0);
				System.out.println("Selected Record: " + record);
				hosID = Integer.parseInt(record.get(0));

				break;
			}catch(Exception e) {
				System.out.println("Invalid input." + e.getMessage());
				continue;
			}
		}while(true);

		//Get department name
		System.out.println("========== Department List ==========");
		try {
			query = "SELECT name FROM Department WHERE hid = " + hosID;
			esql.executeQueryAndPrintResult(query);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		System.out.println("=====================================");

		do {
			try {
				System.out.print("Please enter a departnment name: ");
				departmentName = in.readLine();
				query = String.format("SELECT * FROM Department WHERE hid = %s AND LOWER(name) LIKE ", hosID);
				query += "LOWER('%" + departmentName + "%')";

				result = esql.executeQueryAndReturnResult(query);

				if (result.isEmpty()) {
					System.out.println("Department not found.");
					continue;
				}
				//get department ID
				query = String.format("SELECT dept_ID FROM Department WHERE hid = %s AND LOWER(name) LIKE ", hosID);
				query += "LOWER('%" + departmentName + "%')";
				result = esql.executeQueryAndReturnResult(query);

				if (result.isEmpty()) {
					System.out.println("Could not find department ID");
					continue;
				}

				record = result.get(0);
				System.out.println("Selected Record: " + record);
				depID = Integer.parseInt(record.get(0));

				break;
			}catch(Exception e) {
				System.out.println("Incorrect input. " + e.getMessage());
				continue;
			}
		}while(true);

		//Add doctor
		try {
			query = "INSERT INTO Doctor (doctor_ID, name, specialty, did) VALUES ";
      query += String.format("('%2d', '%s', '%s', '%2d')", docID, dname, special, depID);
      esql.executeUpdate(query);
  	}catch(Exception e) {
          System.out.println("Insert Doctor Query Failed");
    }

		//output new doctor
		try {
			query = "SELECT * FROM Doctor WHERE doctor_ID = " + docID;
			result = esql.executeQueryAndReturnResult(query);
			record = result.get(0);
			System.out.println("Inserted Record: " + record);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public static void AddPatient(DBproject esql) {//2
		int id;
		String name;
		String gender;
		int age;
		String address;
		String query;		

		// Automatically determine patient ID
		do {
			//System.out.print("Please enter the patient id: ");
			try {
				//id = Integer.parseInt(in.readLine());
				query = "SELECT COUNT(patient_ID) FROM Patient";
				List<String> record = esql.executeQueryAndReturnResult(query).get(0);
				int rows = Integer.parseInt(record.get(0));
				//System.out.println("Num record: " + rows);
				id = rows + 1;
				break;
			}catch (Exception e) {
				System.out.println(e.getMessage());
				continue;
			}
		}while (true);

		// Get patient name
		do {
			System.out.print("Please enter the patient name: ");
			try {
				name = in.readLine();
				break;
			}catch (Exception e) {
				System.out.println("Your input must be a string.");
				continue;
			}
		}while (true);

		// Get Gender
		do {
			System.out.print("Please enter the patient's gender M/F: ");
			try {
				gender = in.readLine();
				if (!(gender.equals("M") || gender.equals("F"))) {
					System.out.println("Please enter M or F.");
					continue;
				}
				break;
			}catch (Exception e) {
				System.out.println("Your input must be a character.");
				continue;
			}
		}while (true);

		// Get Age
		do {
			System.out.print("Please enter the patient's age: ");
			try {
				age = Integer.parseInt(in.readLine());
				if ((age < 0) || (age > 130)) {
					System.out.println("Age must be in range: 0 - 130");
					continue;
				}
				break;
			}catch (Exception e) {
				System.out.println("Your input must be an integer.");
				continue;
			}
		}while (true);
		// Get Address
		do {
			System.out.print("Please enter the patient's address: ");
			try {
				address = in.readLine();
				break;
			}catch (Exception e) {
				System.out.println("Your input must be a string.");
				continue;
			}
		}while(true);

		try {
			query = "INSERT INTO Patient (patient_ID, name, gtype, age, address) VALUES ";
			query += String.format("('%2d', '%s', '%s', '%2d', '%s')", id, name, gender, age, address); 
			esql.executeUpdate(query);
		}catch(Exception e) {
			System.out.println("Insert Patient Query Failed" + e.getMessage());
		}

		try {
			query = "SELECT * FROM Patient WHERE patient_ID = " + id;
			List<List<String>> result = esql.executeQueryAndReturnResult(query);
			List<String> record = result.get(0);
			System.out.println("Inserted Record: " + record);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public static void AddAppointment(DBproject esql) {//3
		int appID;
		String appDate;
		String dname;
		String time;
		String stat = "AC";

		int docID;
		String hospitalName;
		int hosID;
		String departmentName;
		int departmentID;

		String query;
		List<List<String>> result;
		List<String> record;

		//Create appointment ID
		do {
			try {
				query = "SELECT COUNT(appnt_ID) FROM Appointment";
				record = esql.executeQueryAndReturnResult(query).get(0);
				appID = Integer.parseInt(record.get(0));
				appID++;
				break;
			}catch (Exception e) {
				System.out.println(e.getMessage());
				continue;
			}
		}while (true);

		//Get Hospital
		System.out.println("========== Hospital List ==========");
		try {
			query = "SELECT name FROM Hospital";
			esql.executeQueryAndPrintResult(query);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		System.out.println("===================================");

		do {
			try {
				System.out.print("Please enter the hospital name: ");
				hospitalName = in.readLine();

				query = "SELECT * FROM Hospital WHERE LOWER(name) LIKE LOWER('%" + hospitalName + "%')";
				result = esql.executeQueryAndReturnResult(query);

				if (result.isEmpty()) {
					System.out.println("Did not find a hospital with that name.");
					continue;
				}

				record = result.get(0);
				System.out.println("Selected Record: " + record);
				hosID = Integer.parseInt(record.get(0));

				break;
			}catch(Exception e) {
				System.out.println("Invalid input." + e.getMessage());
				continue;
			}
		}while(true);

		//Get Department
		System.out.println("========== Department List ==========");
		try {
			query = "SELECT name FROM Department WHERE hid = " + hosID;
			esql.executeQueryAndPrintResult(query);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		System.out.println("=====================================");

		do {
			try {
				System.out.print("Please enter a departnment name: ");
				departmentName = in.readLine();

				query = String.format("SELECT * FROM Department WHERE hid = %s AND LOWER(name) LIKE ", hosID);
				query += "LOWER('%" + departmentName + "%')";

				result = esql.executeQueryAndReturnResult(query);

				if (result.isEmpty()) {
					System.out.println("Did not find a department with that name.");
					continue;
				}

				record = result.get(0);
				System.out.println("Selected Record: " + record);
				departmentID = Integer.parseInt(record.get(0));

				break;
			}catch(Exception e) {
				System.out.println("Incorrect input. " + e.getMessage());
				continue;
			}
		}while(true);

		//Get Doctor
		System.out.println("================= Doctor List ================");
		try {
			query = "SELECT Doc.doctor_ID, Doc.name, Dep.name as department FROM Doctor Doc, Department Dep WHERE Doc.did = Dep.dept_ID ";
			query += String.format("AND Dep.hid = %2d AND Dep.dept_ID = %2d", hosID, departmentID);
			if (esql.executeQuery(query) < 1) {
				System.out.println("There are no doctors in this department");
				return;
			}
			esql.executeQueryAndPrintResult(query);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		System.out.println("===================================");

		do {
			try {
				System.out.print("Please enter the doctor's ID: ");
				docID = Integer.parseInt(in.readLine());

				query = "SELECT * FROM Doctor WHERE doctor_ID = " + docID;
				int rows = esql.executeQuery(query);
				if (rows != 1) {
					System.out.println("That doctor does not exist!");
					continue;
				}
				break;
			}catch(Exception e) {
				System.out.println("Invalid input. " + e.getMessage());
				continue;
			}
		}while(true);

		//Set Appointment time
		System.out.println("========== Current Appointments ==========");
		try {
			query = "SELECT D.doctor_ID, D.name, D.specialty, A.appnt_id, A.status, A.adate, A.time_slot FROM Appointment A, Doctor D, has_appointment H WHERE H.doctor_id = D.doctor_ID AND H.appt_id = A.appnt_ID ";
			query += String.format("AND D.doctor_ID = %2d", docID);
			result = esql.executeQueryAndReturnResult(query);

			if(result.isEmpty()) {
				System.out.println("There are currently no appointments");
			}
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		System.out.println("============================================");

		//Get appointment time
		do{
			try{
				System.out.print("Set an appointment date(MM/DD/YY): ");
				appDate = in.readLine();
				break;
			}catch(Exception e) {
				System.out.println(e.getMessage());
				continue;
			}
		}while(true);

		//get appointment
		try {
			System.out.println("========== Time Slot Options ==========");
			query = "SELECT DISTINCT time_slot FROM Appointment";
			esql.executeQueryAndPrintResult(query);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		System.out.println("============================================");

		do{
			System.out.print("Choose a time_slot: ");
			try{
				time = in.readLine();
				String a = "10:00-17:00";
				String b = "8:00-10:00";
				String c = "10:00-15:00";
				String d = "8:00-10:30";
				String e = "13:00-15:00";
				String f = "14:00-16:00";
				String g = "8:00-17:00";
				String h = "8:00-10:50";

				if(!(time.equals(a) || time.equals(b) || time.equals(c) || time.equals(d) || time.equals(e) || time.equals(f) || time.equals(g) || time.equals(h))) {
					System.out.println("Enter one of the listed time slot options");
					continue;
				}
				break;
			}catch(Exception e) {
				System.out.println(e.getMessage());
				continue;
			}
		}while(true);

		//instert new appointment
		try {
			query = "INSERT INTO Appointment (appnt_ID, adate, time_slot, status) VALUES ";
			query += String.format("('%2d', '%s', '%s', '%s')", appID, appDate, time, stat);
			esql.executeUpdate(query);

			query = "INSERT INTO has_appointment (appt_ID, doctor_id) VALUES ";
			query += String.format("('%2d', '%2d')", appID, docID);
			esql.executeUpdate(query);
		}catch(Exception e) {
			System.out.println("Insert Appointment Query Failed" + e.getMessage());
		}

		try {
			query = "SELECT * FROM Appointment WHERE appnt_ID = " + appID;
			result = esql.executeQueryAndReturnResult(query);
			record = result.get(0);
			System.out.println("Inserted Record: " + record);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}


	public static void MakeAppointment(DBproject esql) {//4
		// Given a patient, a doctor and an appointment of the doctor that s/he wants to take, add an appointment to the DB
		int patientID;
		int doctorID;
		int appointmentID;
		String query;
		String hospitalName;
		String departmentName;
		int hospitalID;
		int departmentID;

		List<List<String>> result;
		List<String> record;

		//#region Get Patient ID
		do {
			try {
				System.out.print("Please enter the patient ID: ");
				patientID = Integer.parseInt(in.readLine());

				query = "SELECT * FROM Patient WHERE patient_ID = " + patientID;
				int rows = esql.executeQuery(query);
				if (rows != 1) {
					System.out.println("That patient does not exist!");
					continue;
				}
				break;
			}catch(Exception e) {
				System.out.println("Input is invalid. " + e.getMessage());
				continue;
			}
		}while(true);
		//#endregion
	
		//#region Print list of hospitals and get hospital name
		System.out.println("========== Hospital List ==========");
		try {
			query = "SELECT name FROM Hospital";
			esql.executeQueryAndPrintResult(query);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		System.out.println("===================================");

		// Get a hospital name (keyword such as UCLA, USC) that is valid
		do {
			try {
				System.out.print("Please enter the hospital name: ");
				hospitalName = in.readLine();

				query = "SELECT * FROM Hospital WHERE LOWER(name) LIKE LOWER('%" + hospitalName + "%')"; 
				result = esql.executeQueryAndReturnResult(query);

				if (result.isEmpty()) {
					System.out.println("Did not find a hospital with that name.");
					continue;
				}

				record = result.get(0);
				System.out.println("Selected Record: " + record);
				hospitalID = Integer.parseInt(record.get(0));

				break;
			}catch(Exception e) {
				System.out.println("Invalid input." + e.getMessage());
				continue;
			}
		}while(true);
		//#endregion
		
		//#region Print out departments for that hospital and get department name
		System.out.println("========== Department List ==========");
		// Print out list of departments for that hospital
		try {
			query = "SELECT name FROM Department WHERE hid = " + hospitalID;
			esql.executeQueryAndPrintResult(query);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		System.out.println("=====================================");

		// Get valid department name for chosen hospital
		do {
			try {
				System.out.print("Please enter a departnment name: ");
				departmentName = in.readLine();

				query = String.format("SELECT * FROM Department WHERE hid = %s AND LOWER(name) LIKE ", hospitalID);
				query += "LOWER('%" + departmentName + "%')";

				//System.out.println("Query = " + query);
				result = esql.executeQueryAndReturnResult(query);

				if (result.isEmpty()) {
					System.out.println("Did not find a department with that name.");
					continue;
				}

				record = result.get(0);
				System.out.println("Selected Record: " + record);
				departmentID = Integer.parseInt(record.get(0));

				break;
			}catch(Exception e) {
				System.out.println("Incorrect input. " + e.getMessage());
				continue;
			}
		}while(true);
		//#endregion
		
		//#region Print out doctor list for that department and get doctor id
		System.out.println("================= Doctor List ================");
		try {
			query = "SELECT Doc.doctor_ID, Doc.name, Dep.name FROM Doctor Doc, Department Dep WHERE Doc.did = Dep.dept_ID ";
			query += String.format("AND Dep.hid = %2d AND Dep.dept_ID = %2d", hospitalID, departmentID);
			if (esql.executeQuery(query) < 1) {
				System.out.println("There are no doctors in this department");
				return;
			}
			esql.executeQueryAndPrintResult(query);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		System.out.println("===================================");

		// get doctor id
		do {
			try {
				System.out.print("Please enter the doctor ID: ");
				doctorID = Integer.parseInt(in.readLine());	

				query = "SELECT * FROM Doctor WHERE doctor_ID = " + doctorID;
				int rows = esql.executeQuery(query);
				if (rows != 1) {
					System.out.println("That doctor does not exist!");
					continue;
				}
				break;
			}catch(Exception e) {
				System.out.println("Invalid input. " + e.getMessage());
				continue;
			}
		}while(true);
		//#endregion

		//#region Print list of that doctor's available appointments 
		System.out.println("========== Available Appointments ==========");
		try {
			query = "SELECT A.appnt_ID, A.adate FROM Appointment A, has_appointment H WHERE H.appt_id = A.appnt_ID AND A.status = 'AV' ";
			query += String.format("AND H.doctor_id = %2d", doctorID);
			esql.executeQueryAndPrintResult(query);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		System.out.println("============================================");

		// get appointment id AND check if it corresponds to the doctor AND check if it is available
		do {
			try {
				// Check if that doc has any at all
				query = "SELECT * FROM Appointment A, has_appointment H WHERE A.appnt_ID = H.appt_id AND A.status = 'AV' ";
				query += String.format("AND H.doctor_id = %2d", doctorID);
				if (esql.executeQuery(query) < 1) {
					System.out.println("This doctor has no appointments available!");
					return;
				}

				System.out.print("Please enter the appointment ID: " );
				appointmentID = Integer.parseInt(in.readLine());
				
				// Check if appointment exists in has_appointment
				query = "SELECT * FROM has_appointment WHERE appt_ID = " + appointmentID;
				result = esql.executeQueryAndReturnResult(query);
				if (result.isEmpty()) {
					System.out.println("The appointment number does not exist!");
					continue;
				}
				record = result.get(0);

				// Check if Doctor ID matches for given appointment
				if (doctorID != Integer.parseInt(record.get(1))) {
					System.out.println("The appointment does not correspond to the doctor!");
					continue;
				}

				// Check if appointment is available
				query = "SELECT * FROM Appointment WHERE appnt_ID = " + appointmentID;
				result = esql.executeQueryAndReturnResult(query);
				record = result.get(0);

				if (!record.get(3).equals("AV")) {
					System.out.println("Appointment is not available!");
					continue;
				}
				break;
			}catch(Exception e) {
				System.out.println("Invalid input." + e.getMessage());
			}
		}while(true);
		//#endregion

		// execute update here
		try {
			query = "UPDATE Appointment SET status = 'AC' WHERE appnt_ID = " + appointmentID;
			esql.executeUpdate(query);
		}catch(Exception e) {
			System.out.println("Here I cry: " + e.getMessage());
		}

		try {
			query = "SELECT * FROM Appointment WHERE appnt_ID = " + appointmentID;
			result = esql.executeQueryAndReturnResult(query);
			record = result.get(0);
			System.out.println("Updated Record: " + record);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		System.out.println("Success!");

	}

	public static void ListAppointmentsOfDoctor(DBproject esql) {//5
		// For a doctor ID and a date range, find the list of active and available appointments of the doctor
		int docID;
		String date1;
		String date2;
		String query;

		List<List<String>> result;
		List<String> record;
		//Get Doctor
		do {
			try {
				System.out.print("Please enter the doctor's ID: ");
				docID = Integer.parseInt(in.readLine());

				query = "SELECT * FROM Doctor WHERE doctor_ID = " + docID;
				result = esql.executeQueryAndReturnResult(query);
				if (result.isEmpty()) {
					System.out.println("That doctor does not exist!");
					continue;
				}

				record = result.get(0);
				System.out.println("Selected Record: " + record);

				break;
			}catch(Exception e) {
				System.out.println("Invalid input. " + e.getMessage());
				continue;
			}
		}while(true);

		//Get Date
		do {
			try {
				System.out.print("Please enter the first date(MM/DD/YY): ");
				date1 = in.readLine();

				break;
			}catch(Exception e) {
				System.out.println("Incorrect input. " + e.getMessage());
				continue;
			}
		}while(true);

		do {
			try {
				System.out.print("Please enter the second date(MM/DD/YY): ");
				date2 = in.readLine();

				break;
			}catch(Exception e) {
				System.out.println("Incorrect input. " + e.getMessage());
				continue;
			}
		}while(true);

	  //Find appointments given dates
		System.out.println("========== Active and Available Appointments ==========");
		try {
			query = "WITH ActAva AS (SELECT D1.doctor_ID, D1.name, D1.specialty, A1.appnt_id, A1.status, A1.adate, A1.time_slot ";
			query += "FROM Appointment A1, Doctor D1, has_appointment H1 WHERE H1.doctor_id = D1.doctor_ID AND H1.appt_id = A1.appnt_ID AND A1.status = 'AV' ";
			query += "UNION SELECT D.doctor_ID, D.name, D.specialty, A.appnt_id, A.status, A.adate, A.time_slot ";
			query += "FROM Appointment A, Doctor D, has_appointment H WHERE H.doctor_id = D.doctor_ID AND H.appt_id = A.appnt_ID AND A.status = 'AC') ";
			query += "SELECT * FROM ActAva R WHERE R.doctor_ID = " + docID;
			query += " AND R.adate BETWEEN '" + date1;
			query += "' AND '" + date2;
			query += "'";
			esql.executeQueryAndPrintResult(query);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}

		System.out.println("==========================================");
	}

	public static void ListAvailableAppointmentsOfDepartment(DBproject esql) {//6
		// For a department name and a specific date, find the list of available appointments of the department
		String hospitalName;
		int hospitalID;
		String departmentName;
		int departmentID;
		String date;
		String query;

		List<List<String>> result;
		List<String> record;

		// Print out list of hospitals
		System.out.println("========== Hospital List ==========");
		try {
			query = "SELECT name FROM Hospital";
			esql.executeQueryAndPrintResult(query);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		System.out.println("===================================");

		// Get a hospital name (keyword such as UCLA, USC) that is valid
		do {
			try {
				System.out.print("Please enter the hospital name: ");
				hospitalName = in.readLine();

				query = "SELECT * FROM Hospital WHERE LOWER(name) LIKE LOWER('%" + hospitalName + "%')"; 
				result = esql.executeQueryAndReturnResult(query);

				if (result.isEmpty()) {
					System.out.println("Did not find a hospital with that name.");
					continue;
				}

				record = result.get(0);
				System.out.println("Selected Record: " + record);
				hospitalID = Integer.parseInt(record.get(0));

				break;
			}catch(Exception e) {
				System.out.println("Invalid input." + e.getMessage());
				continue;
			}
		}while(true);

		System.out.println("========== Department List ==========");
		// Print out list of departments for that hospital
		try {
			query = "SELECT name FROM Department WHERE hid = " + hospitalID;
			esql.executeQueryAndPrintResult(query);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		System.out.println("=====================================");

		// Get valid department name for chosen hospital
		do {
			try {
				System.out.print("Please enter a departnment name: ");
				departmentName = in.readLine();

				query = String.format("SELECT * FROM Department WHERE hid = %s AND LOWER(name) LIKE ", hospitalID);
				query += "LOWER('%" + departmentName + "%')";

				//System.out.println("Query = " + query);
				result = esql.executeQueryAndReturnResult(query);

				if (result.isEmpty()) {
					System.out.println("Did not find a department with that name.");
					continue;
				}

				record = result.get(0);
				System.out.println("Selected Record: " + record);
				departmentID = Integer.parseInt(record.get(0));

				break;
			}catch(Exception e) {
				System.out.println("Incorrect input. " + e.getMessage());
				continue;
			}
		}while(true);

		// Display available dates for that department
		System.out.println("========== Available Dates List ==========");
		try {
			query = "SELECT A.adate FROM Appointment A, Doctor D, has_appointment H WHERE A.status = 'AV' ";
			query += String.format("AND D.did = %2d AND H.doctor_id = D.doctor_ID and H.appt_id = A.appnt_ID", departmentID);
			if (esql.executeQuery(query) < 1) {
				System.out.println("There are no available dates.");
				return;
			}
			esql.executeQueryAndPrintResult(query);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		System.out.println("==========================================");

		// Get a date
		do {
			try {
				System.out.print("Please enter the data. MM/DD/YY: ");
				date = in.readLine();

				break;
			}catch(Exception e) {
				System.out.println("Incorrect input. " + e.getMessage());
				continue;
			}
		}while(true);

		// Query
		try {
			query = "SELECT * FROM Appointment A, Doctor D, has_appointment H WHERE A.status = 'AV' AND A.adate = '" + date + "' ";
			query += String.format("AND D.did = %2d AND H.doctor_id = D.doctor_ID AND H.appt_id = A.appnt_ID", departmentID);
			esql.executeQueryAndPrintResult(query);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public static void ListStatusNumberOfAppointmentsPerDoctor(DBproject esql) {//7
		// Count number of different types of appointments per doctors and list them in descending order
		String query;
		List<List<String>> results;
		List<String> record;

		try {
			query = "WITH Stat AS (SELECT DISTINCT A.status, D.name FROM Doctor D, has_appointment H, Appointment A WHERE D.doctor_ID = H.doctor_id AND H.appt_id = A.appnt_id ";
			query += "GROUP BY A.status, D.name ORDER BY D.name DESC)";
			query += "SELECT Stat.name, COUNT(*) as appointmentTypes FROM Stat GROUP BY Stat.name ORDER BY Stat.name DESC";
			esql.executeQueryAndPrintResult(query);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}

	
	public static void FindPatientsCountWithStatus(DBproject esql) {//8
		// Find how many patients per doctor there are with a given status (i.e. PA, AC, AV, WL) and list that number per doctor.
		String status;
		String query;

		List<List<String>> results;
		List<String> record;

		// Get the status
		do {
			try {
				System.out.print("Please enter the appointment status (PA, AC, AV, WL): ");
				status = in.readLine();
				if (!(status.equals("PA") || status.equals("AC") || status.equals("AV") || status.equals("WL"))) {
					System.out.println("Not a valid input!");
					continue;
				}
				break;
			}catch(Exception e) {
				System.out.println(e.getMessage());
				continue;
			}
		}while(true);

		// Query
		try {
			query = "SELECT D.name, COUNT(A.appnt_ID) AS num_patients FROM Appointment A, Doctor D, has_appointment H ";
			query += "WHERE D.doctor_ID = H.doctor_id AND H.appt_id = A.appnt_ID AND A.status = '" + status + "' ";
			query += "GROUP BY D.name, D.did ORDER BY num_patients DESC";
			//System.out.println("query: " + query);
			esql.executeQueryAndPrintResult(query);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
