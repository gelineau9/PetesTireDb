import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class PetesTireDb {
	//global variable section
	/* QueryParams is a 2d array including all information for queries stored within it
     * Object indices are lined up with DB counterparts, indexing not necessary
     * Each individual array is equivalent to a certain SQL Query
     * Index 0: Insert entire excel into newtire
     * Index 1: Insert entire excel into usedtire
     * Index 2: Insert line into newtire
     * Index 3: Insert line into usedtire
     * Index 4: Remove from newtire using PK 
     * Index 5: Remove from usedtire using PK 
     * Index 6: Find information for a successful login
     * Index 7: Add a user with a certain accesslevel
     * index 8: Delete a user with x password user combo
     * Index 9: Select table and order by certain attribute
     * index 10: Search newtire for specific tire 
     * index 11: Search usedtire for specific tire 
     */
    private static Object[][] queryParams = {
    		{
    			// Loads data in for a newtire from an excel spreadsheet
    			// Filepath to excel spreadsheet should be here
    			"",
    			// Sheet name
    			"NewTire",
    			// SQL query to insert to newtire
    			"INSERT INTO newtire (brand, Rnum, size, label, quantity, extratag) values (?,?,?,?,?,?)",
    			// The filetypes for the 6 values (brand,Rnum,size,label,quantity,extratag)
				// Queries that have a request body are always are coupled with a String[] of the corresponding datatypes to the values in the query
    			new String[] {"String", "int", "String", "String", "int", "String"}
    		},
    		{
    			"",
    			"UsedTire",
    			"INSERT INTO usedtire (tireID, Rnum, size, dateAquired, monthsUsed) values (?,?,?,?,?) ",
    			new String[] {"int", "int", "String", "Date", "int"}
    		},
    		{
				// INSERT statement is used if the tire does not already exists to add new record
    			"INSERT INTO newtire (brand, Rnum, size, label, quantity, extratag) values (?,?,?,?,?,?)",
    			// SELECT statement is used to determine if the tire already exists
    			"SELECT * FROM newtire WHERE brand = ? and Rnum = ? and size = ? and label = ?",
    			// UPDATE statement is used if the tire exists, rather than inserting new tire record
    			"UPDATE newtire SET quantity = quantity+1 WHERE brand = ? and Rnum = ? and size = ? and label = ?",
    			// Datatypes corresponding to the INSERT query
    			new String[] {"String", "int", "String", "String", "int", "String"},
    			// Datatypes corresponding to the SELECT and UPDATE queries
    			new String[] {"String", "int", "String", "String"}
    		},
    		{
    			"INSERT INTO usedtire (tireID, Rnum, size, dateAquired, monthsUsed) values (?,?,?,?,?) ",
    			new String[] {"int", "int", "String", "Date", "int"}
    		},
    		{
    			// DELETE statement is used to remove the record if its quantity reaches 0
    			"DELETE FROM newtire WHERE brand = ? and Rnum = ? and size = ? and label = ?",
    			// SELECT statement is used to determine what the quantity of the tire is
    			"SELECT quantity FROM newtire WHERE brand = ? and Rnum = ? and size = ? and label = ?",
    			// UPDATE statement used to decrease the quantity of a new tire if it exists, rather than removing the whole record
    			"UPDATE newtire SET quantity = quantity-1 WHERE brand = ? and Rnum = ? and size = ? and label = ?",
    			new String[] {"String", "int", "String", "String"}	
    		},
    		{
    			"DELETE FROM usedtire WHERE tireId = ?",
    			new String[] {"int"}
    		},
    		{
    			//This array contains a user that has permissions to view users table for logging in
    			new Object[] {"demo", "demoPass", 0},
    			"SELECT * FROM users WHERE userName = ? and pass = ?",
    			new String[] {"String", "String"}
    					
    		},
    		{
    			"INSERT INTO users (userName, pass, accessLevel) values (?,?,?)",
    			new String[] {"String", "String", "int"}
    		},
    		{
    			"DELETE FROM users WHERE userName = ?",
    			new String[] {"String"}	
    		},
    		{
    			//SELECT query first half
    			"SELECT * from ",
    			//SELECT query second half
    			" ORDER BY "
    		},
    		{
    			"SELECT * FROM newtire WHERE brand = ? and Rnum = ? and size = ? and label = ?",
    			new String[] {"String", "int", "String", "String"}
    		},
    		{
    			"SELECT * FROM usedtire WHERE tireID = ?",
    			new String[] {"int"}
    		}
   		
    };
    
    /*
     * Format for exporting data into excel spreadsheet to backup database
     * Index 0: Sheet Name
     * Index 1: Necessary Select Query
     * Index 2: Array of header names for constructing an excel spreadsheet
     * Index 3: Determines datatypes for the SQL query
     */
    private static Object[][] exportList = {
			{	
				"NewTire",
				"SELECT * FROM newtire ORDER BY Rnum;",
				new String[] {"brand","Rnum", "size", "label", "quantity", "extratag"},
				new String[] {"String","int","String","String","int","String"}
			},
			{
				"UsedTire",
				"SELECT * FROM usedtire ORDER BY tireID;",
				new String[] {"tireID","Rnum", "size", "dateAquired", "monthsUsed"},
				new String[] {"int","int","String","Date","int"},
				
			}
    };
    
    //Initializes loginInfo with a demo user
    public static Object[] loginInfo =(Object[]) queryParams[6][0];

	//Below are the two variables that control the program.
    public static Object[] queryInput = {};
	//To use, select the function you wish to use based on the functions listed.

    
	public static printFunctionList(int accessLevel){
		if(accessLevel>=1){
			System.out.println("1. Enter: 'select'");
			System.out.println("2. Enter: 'searchNew'");
			System.out.println("3. Enter: 'searchUsed'");
		}
		if(accessLevel>=2){
			System.out.println("4. Enter: 'insertNew'");
			System.out.println("5. Enter: 'insertUsed'");
			System.out.println("6. Enter: 'deleteNew'");
			System.out.println("7. Enter: 'deleteUsed'");
		}
		if(accessLevel>=3){
			System.out.println("8. Enter: 'loadExcel'");
			System.out.println("9. Enter: 'saveExcel'");
		}
		if(accessLevel>=4){
			System.out.println("10. Enter: 'createUser'");
			System.out.println("11. Enter: 'grantPrivileges'");
			System.out.println("12. Enter: 'deleteUser'");
		}
	}

    public static void main(String[] args) throws SQLException, IOException{
    	login();
		Scanner inputScan = new Scanner(System.in); 
		System.out.println("Choose your function from the list below");
		printFunctionList(loginInfo[2]);
    	String function = inputScan.nextLine();

    	/* select statements access level 1
    	 insertion and deletion access level 2
    	 saving and loading access level 3
    	 managing users access level 4
		*/

    	if((Integer) loginInfo[2]>=1) {
    		//selects from any table, requires 2 values in queryInput: table name, and column name
    		if(function.equals("select")) {
				System.out.println("For the SELECT query 2 inputs are required.");
				System.out.println("Enter table name");
				queryInput.append(inputScan.nextline());
				System.out.println("Enter column name");
				queryInput.append(inputScan.nextline());

        		PreparedStatement selectStatement = getConnection((String) loginInfo[0],(String) loginInfo[1]).prepareStatement((String) queryParams[9][0]+queryInput[0]+queryParams[9][1]+queryInput[1]);
        		outputSelectStatement(selectStatement);
        	}
    		//searches newtire table for a specific value, requires 4 values in queryInput: brand, rnum, size, and label
    		else if(function.equals("searchNew")) {
				System.out.println("For the SEARCHNEW query 4 inputs are required.");
				System.out.println("Enter brand name");
				queryInput.append(inputScan.nextline());
				System.out.println("Enter r number");
				queryInput.append(inputScan.nextline());
				System.out.println("Enter tire size");
				queryInput.append(inputScan.nextline());
				System.out.println("Enter the tire label");
				queryInput.append(inputScan.nextline());

    			PreparedStatement selectStatement = getConnection((String) loginInfo[0],(String) loginInfo[1]).prepareStatement((String) queryParams[10][0]);
    			populatePreparedStatement(selectStatement, (String[]) queryParams[10][1], queryInput);
        		outputSelectStatement(selectStatement);
    		}
    		//searches usedtire table for a specific value, requires 1 value in queryInput: tireID
    		else if(function.equals("searchUsed")) {
				System.out.println("For the SEARCHUSED query 1 input is required.");
				System.out.println("Enter the tire ID");
				queryInput.append(inputScan.nextline());

    			PreparedStatement selectStatement = getConnection((String) loginInfo[0],(String) loginInfo[1]).prepareStatement((String) queryParams[11][0]);
    			populatePreparedStatement(selectStatement, (String[]) queryParams[11][1], queryInput);
        		outputSelectStatement(selectStatement);
    		}
    	}
    	if((Integer) loginInfo[2]>=2) {
    		//inserts new tire into newtire, requires 6 values in queryInput: brand, rnum, size, label, quantity, and extraTag
    		if(function.equals("insertNew")) {
				System.out.println("For the INSERTNEW query 6 inputs are required.");
				System.out.println("Enter brand name");
				queryInput.append(inputScan.nextline());
				System.out.println("Enter r number");
				queryInput.append(inputScan.nextline());
				System.out.println("Enter tire size");
				queryInput.append(inputScan.nextline());
				System.out.println("Enter the tire label");
				queryInput.append(inputScan.nextline());
				System.out.println("Enter quantity");
				queryInput.append(inputScan.nextline());
				System.out.println("Enter extra tag");
				queryInput.append(inputScan.nextline());

        		insertNewTire(getConnection((String) loginInfo[0],(String) loginInfo[1]), queryParams[2], queryInput);
        	}
    		//inserts used tire into usedtire, utilizes try catch if tireId is already present in the DB, requires 5 value in queryInput: tireID, rnum, size, dateAquired, and monthsUsed
        	else if(function.equals("insertUsed")) {
        		try {
					System.out.println("For the INSERTUSED query 5 inputs are required.");
					System.out.println("Enter tireID");
					queryInput.append(inputScan.nextline());
					System.out.println("Enter r number");
					queryInput.append(inputScan.nextline());
					System.out.println("Enter tire size");
					queryInput.append(inputScan.nextline());
					System.out.println("Enter the date acquired");
					queryInput.append(inputScan.nextline());
					System.out.println("Enter months used");
					queryInput.append(inputScan.nextline());

        			PreparedStatement insertStatement = getConnection((String) loginInfo[0],(String) loginInfo[1]).prepareStatement((String) queryParams[3][0]);
                	populatePreparedStatement(insertStatement, (String[]) queryParams[3][1], queryInput);
                	insertStatement.execute();
                	insertStatement.close();
        		}
        		catch(SQLException e) {
        			System.out.println("This tire already exists in the database with that tireID");
        		}
        	}
    		//deletes new tire from newtire db, requires 4 values in queryInput: brand, rnum, size, label
        	else if(function.equals("deleteNew")) {
				System.out.println("For the DELETENEW query 4 inputs are required.");
				System.out.println("Enter brand name");
				queryInput.append(inputScan.nextline());
				System.out.println("Enter r number");
				queryInput.append(inputScan.nextline());
				System.out.println("Enter tire size");
				queryInput.append(inputScan.nextline());
				System.out.println("Enter the tire label");
				queryInput.append(inputScan.nextline());

        		deleteNewTire(getConnection((String) loginInfo[0],(String) loginInfo[1]), queryParams[4], queryInput);
        	}
    		//deletes used tire from usedtire db, utilizing try/catch if tireID does not exist, requires 1 value in queryInput: tireID
        	else if(function.equals("deleteUsed")) {
        		try {
					System.out.println("For the DELETEUSED query 1 input is required.");
					System.out.println("Enter the tire ID");
					queryInput.append(inputScan.nextline());

        			PreparedStatement deleteStatement = getConnection((String) loginInfo[0],(String) loginInfo[1]).prepareStatement((String) queryParams[5][0]);
            		populatePreparedStatement(deleteStatement, (String[]) queryParams[5][1], queryInput);
            		deleteStatement.execute();
            		deleteStatement.close();
        		}
        		catch(SQLException e) {
        			System.out.println("This tire does not exist in the database with that tireID");
        		}
        		
        	}
    	}
    	if((Integer) loginInfo[2]>=3) {
    		//loads information from the chosen sheet of an excel file into the DB, populating it. requires 1 value in queryInput: tablename
    		//used to truncate the table in the chosen table and then load in a saved state of the data through an excel sheet
    		if(function.equals("loadExcel")) {
				System.out.println("For the LOADEXCEL query 1 input is required.");
				System.out.println("Enter the table name");
				queryInput.append(inputScan.nextline());

    			PreparedStatement truncate = getConnection((String) loginInfo[0],(String) loginInfo[1]).prepareStatement("TRUNCATE TABLE " + queryInput[0]);
        		truncate.execute();
        		if(((String) queryInput[0]).toUpperCase().equals("NEWTIRE")) {
        			loadExcel(getConnection((String) loginInfo[0],(String) loginInfo[1]), queryParams[0]);
        		}
        		else if(((String) queryInput[0]).toUpperCase().equals("USEDTIRE")) {
        			loadExcel(getConnection((String) loginInfo[0],(String) loginInfo[1]), queryParams[1]);
        		}
        		truncate.close();
    		}
        	//saves current tables to a new excel sheet at the chosen file location
        	//requires 1 value in queryInput: file path to save chosen excel sheet
        	else if(function.equals("saveExcel")) {
				System.out.println("For the SAVEEXCEL query 1 input is required.");
				System.out.println("Enter the full file path to save (including excel sheet filename)");
				queryInput.append(inputScan.nextline());

    		    saveExcel(getConnection((String) loginInfo[0],(String) loginInfo[1]), exportList, (String) queryInput[0]);
        	}
    	}
    	if((Integer) loginInfo[2]>=4) {
    		//creates a user, requires 3 values in queryInput: userName, pass, and accessLevel
    		if(function.equals("createUser")) {
				System.out.println("For the CREATEUSER query 3 inputs are required.");
				System.out.println("Enter the desired username");
				queryInput.append(inputScan.nextline());
				System.out.println("Enter the desired password");
				queryInput.append(inputScan.nextline());
				System.out.println("Enter the desired accessLevel");
				queryInput.append(inputScan.nextline());

        		createUser(getConnection((String) loginInfo[0],(String) loginInfo[1]), queryParams[7], queryInput);
        	}
    		//updates a user's privileges, requires 3 values in queryInput: userName, pass, and accessLevel
        	else if(function.equals("grantPrivileges")) {
				System.out.println("For the GRANTPRIVILEGES query 3 inputs are required.");
				System.out.println("Enter the username");
				queryInput.append(inputScan.nextline());
				System.out.println("Enter the password");
				queryInput.append(inputScan.nextline());
				System.out.println("Enter the current accessLevel");
				queryInput.append(inputScan.nextline());

        		grantPrivileges(getConnection((String) loginInfo[0],(String) loginInfo[1]), queryInput);
        	}
    		//deletes a user from the DB, requires 1 value: userName
        	else if(function.equals("deleteUser")) {
				System.out.println("For the DELETEUSER query 1 input is required.");
				System.out.println("Enter the username of the user you wish to delete");
				queryInput.append(inputScan.nextline());
				
        		deleteUser(getConnection((String) loginInfo[0],(String) loginInfo[1]), queryParams[8], queryInput);
        	}
    	}
		inputScan.close();
    }
    
    public static Connection getConnection(String userName, String pass) throws SQLException {
        Connection conn = null;
        try {
		    String url       = "jdbc:mysql://localhost:yourPort/yourDb";
		    String user      = userName;
		    String password  = pass;
		    
		    conn = DriverManager.getConnection(url, user, password);   
		    } 
        catch(SQLException e) {
		    System.out.println(e.getMessage());
			}
		return conn; 
   }
    
    public static int getLogin(Connection conn, Object[] loginParams, String userName, String pass) throws IOException, SQLException {
    	int accessLevel = 0;
    	String[] loginVals = {userName, pass};
    	PreparedStatement selectStatement = conn.prepareStatement((String) loginParams[1]);
    	populatePreparedStatement(selectStatement, (String[]) loginParams[2], loginVals);
    	ResultSet rs = selectStatement.executeQuery();
    	rs.next();
    	try {
    		if(rs.getString(1).equals(userName)) {
    			if(rs.getString(2).equals(pass)) {
    				accessLevel=rs.getInt(3);
    			}
        	}
    	}
    	catch(SQLException e){
    		System.out.println("User does not exist");
    	}
    	rs.close();
    	selectStatement.close();
    	return accessLevel;
    }
    
    public static void login() throws IOException, SQLException {
    	boolean loggedIn = false;
    	int loginFails = 0;
    	int loginAccess = 0;
    	while(!loggedIn) {
    		if(loginFails<=6) {
    			Scanner inputScan = new Scanner(System.in); 
        	    System.out.println("Enter username");

        	    String userName = inputScan.nextLine(); 
        	    System.out.println("Enter password");

        	    String pass = inputScan.nextLine(); 
        	    
        	    loginAccess = getLogin(getConnection((String) loginInfo[0],(String) loginInfo[1]), queryParams[6], userName, pass);
        	    if(loginAccess>0) {
        	    	inputScan.close();
        	    	loginInfo[0] = userName;
        	    	loginInfo[1] = pass;
        	    	loginInfo[2] = loginAccess;
        	    	loggedIn=true;
        	    }
        	    else {
        	    	System.out.println("That login combination does not exist, please try again.");
        	    	loginFails++;
        	    }
    		}
    		else {
    			System.out.println("You have failed to log in too many times, shutting down");
    			System.exit(0);
    		}
    	}
    }
    
    public static void createUser(Connection conn, Object[] query, Object[] userInfo) throws SQLException, IOException {    	
    	try {
    		PreparedStatement insertStatement = conn.prepareStatement((String) query[0]);
    		populatePreparedStatement(insertStatement, (String[]) query[1], userInfo);
        	insertStatement.execute();
        	insertStatement.close();
    	}
    	catch(SQLException e) {
    		System.out.println("This user already exists");
    		return;
    	}
    	PreparedStatement createStatement = conn.prepareStatement("CREATE USER IF NOT EXISTS " + userInfo[0] + " IDENTIFIED BY ?");
    	createStatement.setString(1, (String) userInfo[1]);
    	createStatement.execute();
    	grantPrivileges(conn, userInfo);
    	createStatement.close();
    }
    
    public static void deleteUser(Connection conn, Object[] query, Object[] userInfo) throws SQLException, IOException {
    	PreparedStatement deleteStatement = conn.prepareStatement((String) query[0]);
    	try {
    		populatePreparedStatement(deleteStatement, (String[]) query[1], userInfo);
        	deleteStatement.execute();
        	deleteStatement.close();
    	}
    	catch(SQLException e) {
    		System.out.println("This user does not exist");
    		return;
    	}
    	PreparedStatement dropStatement = conn.prepareStatement("DROP USER IF EXISTS " + userInfo[0]);
    	dropStatement.execute();
    	dropStatement.close();
    }
    
    public static void grantPrivileges(Connection conn, Object[] userInfo) throws SQLException {
    	PreparedStatement grantPrivileges = conn.prepareStatement("GRANT SELECT ON newtire to " + userInfo[0]);;
    	for(int i=1; i<=((Integer) userInfo[2]); i++){
    		//if statements add privileges based on accesslevel as i increments, for example a level 3 access level gets 2 and 1 as well
    		if(i==1) {
    			grantPrivileges = conn.prepareStatement("GRANT SELECT ON newtire to " + userInfo[0]);
    			grantPrivileges.execute();
    			grantPrivileges = conn.prepareStatement("GRANT SELECT ON usedtire to " + userInfo[0]);
    			grantPrivileges.execute();
    			grantPrivileges = conn.prepareStatement("GRANT SELECT ON users to " + userInfo[0]);
    			grantPrivileges.execute();
    		}
    		else if(i==2) {
    			grantPrivileges = conn.prepareStatement("GRANT INSERT, UPDATE, DELETE ON newtire to " + userInfo[0]);
    			grantPrivileges.execute();
    			grantPrivileges = conn.prepareStatement("GRANT INSERT, UPDATE, DELETE ON usedtire to " + userInfo[0]);
    			grantPrivileges.execute();
    		}
    		else if(i==4) {
    			grantPrivileges = conn.prepareStatement("GRANT ALL PRIVILEGES ON newtire to " + userInfo[0]);
    			grantPrivileges.execute();
    			grantPrivileges = conn.prepareStatement("GRANT ALL PRIVILEGES ON usedtire to " + userInfo[0]);
    			grantPrivileges.execute();
    			grantPrivileges = conn.prepareStatement("GRANT ALL PRIVILEGES ON users to " + userInfo[0]);
    			grantPrivileges.execute();
    		}
    	}
    	grantPrivileges.close();
    }
    
    public static void populatePreparedStatement(PreparedStatement ps, String[] dataType, Object[] psValues) throws IOException, SQLException {
    	for(int i=0; i<dataType.length; i++) {
    		try {
				if(dataType[i].toUpperCase().equals("STRING")) {
    				ps.setString(i+1, (String) psValues[i]);    		
    			}
    			else if(dataType[i].toUpperCase().equals("INT")) {
    				ps.setInt(i+1, (Integer) psValues[i]);
    			}
    			else if(dataType[i].toUpperCase().equals("DATE")) {
    				java.util.Date utilDate = (Date) psValues[i];
    				java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
    				ps.setDate(i+1, sqlDate );
    			}
    			else {
    				throw new SQLException ("Incompatible datatype " + dataType[i]);
    			}
			}
			catch(ArrayIndexOutOfBoundsException e){
				break;
			}
    	}
    }

    public static void insertNewTire(Connection conn, Object[] query, Object[] insertValues) throws SQLException, IOException {
    	//checks if tire exists
		PreparedStatement selectStatement = conn.prepareStatement((String) query[1]);
    	populatePreparedStatement(selectStatement, (String[]) query[4], insertValues);
    	ResultSet rs = selectStatement.executeQuery();
    	selectStatement.close();
    	if(rs.next()) {
    		//if it exists, instead of adding new record, increment the quantity
    		PreparedStatement updateStatement = conn.prepareStatement((String) query[2]);
        	populatePreparedStatement(updateStatement, (String[]) query[4], insertValues);
        	updateStatement.execute();
        	updateStatement.close();
    	}
    	else {
    		//if it does not exist, insert new record
    		PreparedStatement insertStatement = conn.prepareStatement((String) query[0]);
        	populatePreparedStatement(insertStatement, (String[]) query[3], insertValues);
        	insertStatement.execute();
        	insertStatement.close();
    	}
    	rs.close();
    }
    
    public static void deleteNewTire(Connection conn, Object[] query, Object[] deleteValues) throws SQLException, IOException {
    	//removes 1 from the quantity of the selected tire
    	PreparedStatement updateStatement = conn.prepareStatement((String) query[2]);
    	populatePreparedStatement(updateStatement, (String[]) query[3], deleteValues);
    	updateStatement.execute();
    	//selects the quantity value of the tire and returns it to rs
    	PreparedStatement selectStatement = conn.prepareStatement((String) query[1]);
    	populatePreparedStatement(selectStatement, (String[]) query[3], deleteValues);
    	ResultSet rs = selectStatement.executeQuery();
    	rs.next();
    	try {
    		
    		if(rs.getInt(1) <= 0) {
        		PreparedStatement deleteStatement = conn.prepareStatement((String) query[0]);
        		populatePreparedStatement(deleteStatement, (String[]) query[3], deleteValues);
        		deleteStatement.execute();
        		deleteStatement.close();
        	}
    	}
    	catch(SQLException e){
    		System.out.println("This tire does not exist");
    	}
    	
    	rs.close();
    	selectStatement.close();
    	updateStatement.close();
    }
    
    public static void loadExcel(Connection conn, Object[] query) throws IOException, SQLException {
    	PreparedStatement insertStatement = conn.prepareStatement((String) query[2]);
    	FileInputStream fileInput = new FileInputStream(new File((String) query[0]));  	
	    XSSFWorkbook workBook = new XSSFWorkbook(fileInput);
    	//initialize the sheet based on sheetname
    	XSSFSheet sheet = workBook.getSheet((String) query[1]);
    	Iterator<Row> rowIterator = sheet.iterator();
    	rowIterator.next();
    	//uses iterator to determine how many rows the sheet has
    	while (rowIterator.hasNext()) {
    		Row row = rowIterator.next();
    		Iterator<Cell> cellIterator = row.cellIterator();
    		int cellCount = 0;
    		String[] dataType = (String[]) query[3];
    		while (cellIterator.hasNext()) {
    			Cell cell = cellIterator.next();
    			try {
    				if(dataType[cellCount].toUpperCase().equals("STRING") && !(cell.getStringCellValue().equals(" "))) {
	    				insertStatement.setString(cellCount+1, cell.getStringCellValue());	    			}
	    			else if(dataType[cellCount].toUpperCase().equals("INT") && cell.getNumericCellValue() != 0.0 ) {
	    				insertStatement.setInt(cellCount+1, (int) cell.getNumericCellValue());
	    			}
	    			else if(dataType[cellCount].toUpperCase().equals("DATE") && !(cell.getDateCellValue().equals("0000-00-00"))) {
	    				java.util.Date utilDate = cell.getDateCellValue();
	    				java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
	    				insertStatement.setDate(cellCount+1, sqlDate );
	    			}
	    			else {
	    				throw new SQLException ("Incompatible datatype " + dataType[cellCount]);
	    			}
    			}
    			catch(ArrayIndexOutOfBoundsException e){
    				break;
    			}
    			cellCount++;
    		}
    		insertStatement.execute();		
    	}
    	workBook.close();
    }
    
    public static void saveExcel(Connection conn, Object[][] export, String fileLocation) throws SQLException, IOException {
    	XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet;
		PreparedStatement exportStatement = null;
		for(int i=0; i<export.length; i++) {
			int rowCount=0;
			String[] exportDatatype = (String[]) export[i][3];
			String[] exportHeader = (String[]) export[i][2];
			//uses sheet name provided at index 0 of exportList to make sheet
			sheet = workbook.createSheet((String) export[i][0]);
			Row headerRow = sheet.createRow(rowCount);	
			for(int k=0; k < exportHeader.length; k++) {
				Cell headerCell = headerRow.createCell(k);
				headerCell.setCellValue(exportHeader[k]);
			}	
			rowCount++;
			//End header columns/rows/cells, begin data
			exportStatement = conn.prepareStatement((String) export[i][1]);
			ResultSet rs = exportStatement.executeQuery();
			while(rs.next()) {
				Row dataRow = sheet.createRow(rowCount);
				for(int j=0; j < exportHeader.length; j++) {
					Cell dataCell = dataRow.createCell(j);
					if(exportDatatype[j].toUpperCase().equals("INT")) {
						int id = rs.getInt(exportHeader[j]);
						dataCell.setCellValue(id);
					} 
					else if (exportDatatype[j].toUpperCase().equals("STRING") || exportDatatype[j].toUpperCase().equals("DATE")) {
						String data = rs.getString(exportHeader[j]);
						dataCell.setCellValue(data);
					}
				}
				rowCount++;
			}
			rs.close();

		}
		exportStatement.close();
		FileOutputStream fileOutput = new FileOutputStream(new File(fileLocation));
		workbook.write(fileOutput);
		workbook.close();
    }
    
    public static void outputSelectStatement(PreparedStatement ps) throws SQLException {
    	ResultSet rs=ps.executeQuery();
    	String output = "";
    	ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();
    	for(int k=0; k<rsmd.getColumnCount(); k++) {
    		output = output + " " + rsmd.getColumnName(k+1);
    	}
    	System.out.println(output);
    	output = "";
    	//outputs select query data, while there is a new resultset to print
    	while (rs.next()) {
            for(int i=0; i<rsmd.getColumnCount(); i++){
            	output = output + " " + rs.getString(i+1);
            }
            System.out.println(output);
            output = "";
        }
    	ps.close();
    	rs.close();

    }
}
