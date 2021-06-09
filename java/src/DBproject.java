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
	}

	public static void AddPatient(DBproject esql) {//2
		int id;
		String name;
		String gender;
		int age;
		String address;
		String query;		

		// Get patient id
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
				int rows = esql.executeQuery(query);
				if (rows == 0) {
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
			query = "UPDATE Appointment SET status = 'AC' WHERE A.appnt_ID = " + appointmentID;
			esql.executeUpdate(query);
		}catch(Exception e) {
			System.out.println(e.getMessage());
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
				System.out.print("Please enter the appointment status: ");
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