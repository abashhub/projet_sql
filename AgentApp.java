package sql_project;


import java.sql.*;
import java.util.Scanner;

public class AgentApp {
	private Connection conn; // � modifier
	public static final Scanner sc = new Scanner(System.in);
	private PreparedStatement st_listeHeroDisparu;
	private PreparedStatement st_ajouterHero;
	private PreparedStatement st_supprimerHero;
	private PreparedStatement st_chercherHero;
	private PreparedStatement st_authentification;
	private PreparedStatement st_ajoutCombat;
	private PreparedStatement st_ajoutParticipant;
	private PreparedStatement st_selectFaction;
	private PreparedStatement st_ajoutReperage;
	
	public AgentApp() throws SQLException{
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("Driver PostgreSQL manquant !");
			System.exit(1);
		}

		// url at school final
		// String url="jdbc:postgresql://172.24.2.6:5432/dbbberge15?user=abashir15&password=6Sf4Waa";
		
		// url for me at school
		//String url="jdbc:postgresql://172.24.2.6:5432/dbabashir15?user=abashir15&password=6Sf4Waa";
		
		// String url="jdbc:postgresql://172.24.2.6:5432/dbbberge15?user=bberge15&password=C3sUFU8";
		
		// url at home
		String url = "jdbc:postgresql://localhost:5432/projet_sql?user=postgres&password=aTY68Eg";
		
		//String url="jdbc:postgresql://localhost:5432/pubs2?user=postgres&password=root";
		conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.println("Impossible de joindre le server !");
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Connection etablie au server.");
		
		st_listeHeroDisparu = conn.prepareStatement("select * from heroDisparu_view");
				
		st_ajouterHero = conn.prepareStatement("select ajoutHero(?, ?, ?, ?, ?::factions, ?,?)");
		
		st_supprimerHero = conn.prepareStatement("select supressionHero(?)");
		
		st_chercherHero = conn.prepareStatement("select h.* from shyeld.heros h where h.nom_hero = ?;");
		
		st_authentification = conn.prepareStatement("select a.password from shyeld.agents a where a.code_name = ? AND true=a.actif;");
		
		st_ajoutCombat = conn.prepareStatement("select ajoutCombat(?,?,?::DATE)");
		
		st_ajoutParticipant = conn.prepareStatement("select ajoutParticipants(?,?,?);");
		
		st_selectFaction = conn.prepareStatement("select h.faction from shyeld.heros h where h.nom_hero= ? AND true=h.en_vie;");
		
		st_ajoutReperage = conn.prepareStatement("select ajoutReperage(?, ?, ?::DATE, ?, ?);");
		
	}
	
	public static void main(String[] args) {
		AgentApp agentApp=null;
		try {
			agentApp = new AgentApp();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		String agent = "";
		boolean authentifie = false;
		while (authentifie != true) {
			System.out.print("Login: ");
			agent = sc.nextLine();
			System.out.print("Password: ");
			String password = sc.nextLine();
			try {
				authentifie = agentApp.estAuthentifie(agent, password);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Bienvenu agent " + agent);

		System.out.println("Bienvenu dans l'application principale.");
		while (true) {
			System.out.println(
					"Options: 1.Ajouter un h�ro, 2. supprimer un h�ro, 3. chercher un h�ro 4.Enregistrer un reperage 5. Rapporter un combat 6.Lister h�ros disparus (ne contient pas les h�ros qui n'ont jamais eu de rep�rage) 7.Quitter ");
			System.out.println("Veuillez s�lectionner un op�ration � effectuer:  ");
			int choix = sc.nextInt();
			try{
				switch (choix) {
				case 1:
					agentApp.ajouterHero();
					break;
				case 2:
					agentApp.supprimerHero();
					break;
				case 3:
					agentApp.chercherHero();
					break;
				case 4:
					agentApp.enregistrerApparition(agent);
					break;
				case 5:
					agentApp.rapporterUnCombat(agent);
					break;
				case 6:
					agentApp.listeHeroDisparu();
					break;
				case 7:
					System.exit(0);
				default:
					System.out.println("erreur");
	
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println(e);
			}finally{
				System.out.println("\n mais poursuivons");
			}
		}

	}

	private void listeHeroDisparu() {
		
		try(ResultSet rs= st_listeHeroDisparu.executeQuery()){
			while(rs.next()) {
				System.out.println(rs.getString(1));
			}
			}catch (SQLException e) {
				e.printStackTrace();
			}
		
	}

	public boolean estAuthentifie(String login, String mdp) throws SQLException {
		
			//Statement s = conn.createStatement();
			/*try (ResultSet rs = s.executeQuery("SELECT verifie('" + login + "','" + mdp + "');")) {
				rs.next();
				return rs.getString(1).equals("t");
			}*/
			
			st_authentification.setString(1, login);
			ResultSet rs = st_authentification.executeQuery();
				if(rs.next())
				//return rs.getString(1).equals("t");
					return BCrypt.checkpw(mdp, rs.getString(1));
				else
					return false;
			
			
			
			
		
		
	}

	public  void ajouterHero() throws SQLException {
		System.out.println("---INSCRIRE UN HERO---");
		String nom_hero, adresse, origine, faction, pouvoir, puissance, nom_civil;
		sc.nextLine();
		System.out.println("Ins�rez le nom de h�ro:");
		nom_hero = sc.nextLine();
		System.out.println("Ins�rez le nom civil du h�ro:");
		nom_civil = sc.nextLine();
		System.out.println("Ins�rez son adresse:");
		adresse = sc.nextLine();
		System.out.println("Ins�rez son origine:");
		origine = sc.nextLine();
		System.out.println("Ins�rez la faction:");
		faction = sc.nextLine();
		System.out.println("Ins�rez son pouvoir:");
		pouvoir = sc.nextLine();
		System.out.println("Ins�rez son puissance:");
		puissance = sc.nextLine();

		
			//Statement s = conn.createStatement();
			
			st_ajouterHero.setString(1, nom_civil);
			st_ajouterHero.setString(2, nom_hero);
			st_ajouterHero.setString(3, adresse);
			st_ajouterHero.setString(4, origine);
			st_ajouterHero.setString(5, faction);
			st_ajouterHero.setString(6, pouvoir);
			st_ajouterHero.setString(7, puissance);
			st_ajouterHero.executeQuery();
			
			// executeUpdate()
		
		System.out.println("Le h�ro a �t� inscrit");
		System.out.println();

	}

	public void supprimerHero() throws SQLException {
		System.out.println("---SUPPRIMER HERO---");
		System.out.println("Ins�rez le nom de h�ro:");
		sc.nextLine();
		String nom_hero = sc.nextLine();
			//Statement s = conn.createStatement();
			
			st_supprimerHero.setString(1, nom_hero);
			st_supprimerHero.executeQuery();
			
		System.out.println("Le h�ro a �t� indiqu� comme innactif");
		System.out.println();
	}

	public void chercherHero() throws SQLException {
		System.out.println("---CHERCHER HERO---");
		sc.nextLine();
		System.out.println("Ins�rez le nom de h�ro:");
		String nom_hero = sc.nextLine();

		
			//Statement s = conn.createStatement();
			
			st_chercherHero.setString(1, nom_hero);
			ResultSet rs = st_chercherHero.executeQuery();
				while (rs.next()) {
					System.out.println();
					System.out.print("nom civil: " + rs.getString(2));
					System.out.print(" |nom de h�ro: " + rs.getString(3));
					System.out.print(" |adresse: " + rs.getString(4));
					System.out.print(" |origine: " + rs.getString(5));
					System.out.print(" |faction: " + rs.getString(6));
					if(rs.getString(7).equals("t"))
						System.out.print(" |en vie: true");
					else
						System.out.print(" |en vie: false");
					System.out.print(" |pouvoir: " + rs.getString(8));
					System.out.print(" |puissance: " + rs.getString(9));
					System.out.println();
				
				}
				//DBTablePrinter.printResultSet(rs);
			
		
	}

	public void enregistrerApparition(String agent) throws SQLException {
		System.out.println("---ENREGISTRER REPERAGE---");
		sc.nextLine();
		System.out.println("Ins�rez le nom de h�ro:");
		String nom_hero = sc.nextLine();
		System.out.println("Ins�rez la date  (yyyy-mm-dd): ");
		String date = sc.nextLine();
		System.out.println("Ins�rez coordonn�e X:");
		int coord_x = sc.nextInt();
		System.out.println("Ins�rez coordonn�e Y:");
		int coord_y = sc.nextInt();
		
		
			st_ajoutReperage.setString(1, nom_hero);
			st_ajoutReperage.setString(2, agent);
			st_ajoutReperage.setString(3, date);
			st_ajoutReperage.setInt(4, coord_x);
			st_ajoutReperage.setInt(5, coord_y);
			st_ajoutReperage.execute();

		
		System.out.println("L'apparition a �t� enregistr�e");

	}

	public void rapporterUnCombat(String agent) throws SQLException {
		int x, y;
		String date;
		System.out.println("Entrez la coorodn�e x du combat:");
        x=sc.nextInt();
        System.out.println("Entrez la coorodn�e y du combat:");
        y=sc.nextInt();
        System.out.println("Entrez la date du combat (yyyy-mm-dd):");
        date=sc.next();

        
		
		 
		        conn.setAutoCommit(false);
		       // prepare statement
	            st_ajoutCombat.setInt(1, x);
	            st_ajoutCombat.setInt(2, y);
	            st_ajoutCombat.setString(3, date);
	            ResultSet resultSet=st_ajoutCombat.executeQuery();
	            resultSet.next();
	            int idCombat = resultSet.getInt(1);
	            System.out.println("Veuillez maintenant ajouter les participants");
		        String rep="";
		        
		        String faction = null; 
		        boolean bol=false;
		        while (!rep.equals("non")) {
		        	String nomHero, resultat;
		        	sc.nextLine();
		        	System.out.println("Entrez le nom du h�ro:");
		        	nomHero=sc.nextLine();
		        	System.out.println("Entrez le r�sultat ('gagnant','perdant','autre'):");
		        	resultat=sc.nextLine();
		        	st_ajoutParticipant.setString(1, nomHero);
		        	st_ajoutParticipant.setInt(2, idCombat);
		        	st_ajoutParticipant.setString(3,resultat );
		        	resultSet =st_ajoutParticipant.executeQuery();
		        	st_ajoutReperage.setString(1, nomHero);
		        	st_ajoutReperage.setString(2, agent);
		        	st_ajoutReperage.setString(3, date);
		        	st_ajoutReperage.setInt(4, x);
		        	st_ajoutReperage.setInt(5, y);
		        	st_ajoutReperage.execute();
		        	st_selectFaction.setString(1, nomHero);
		        	resultSet =st_selectFaction.executeQuery();
		        	resultSet.next();
		        	String temp=resultSet.getString(1);
		        	if(faction==null){
		        		faction=temp;
		        	}
		        	if(!faction.equals(temp)){
		        		bol=true;
		        	}
		        	System.out.println("Voulez vous entrer un autre participant? (�crivez non pour arr�ter, n'importe quoi d'autre pour continuer)");
		        	rep=sc.nextLine();
		        }
		        if(bol==false){
		        	 System.err.print("Transaction is being rolled back");
		             conn.rollback();
		        }else{
		        	conn.setAutoCommit(true);
		        }
		        
		        //conn.commit();
		    

	}
}
