package sql_project;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;


public class Central {
	private Connection conn;//à modifier
	static Scanner sc;
	private PreparedStatement st_ajoutAgent;
	private PreparedStatement st_supressionAgent;
	private PreparedStatement st_listeRisque;
	private PreparedStatement st_historiqueAgent;
	private PreparedStatement st_historiqueCombat;
	private PreparedStatement st_classementHeroVictoires;
	private PreparedStatement st_classementHerosDefaites;
	private PreparedStatement st_classementAgents;
	
	Central() {
		sc = new Scanner(System.in);
		
		//On charge le driver
				try {
						Class.forName("org.postgresql.Driver");
					}catch (ClassNotFoundException e) {
						System.out.println("Driver PostgreSQL manquant !");
						System.exit(1);
					}
				//On obtient la connection
				// final for school
				//String url="jdbc:postgresql://172.24.2.6:5432/dbbberge15?user=abashir15&password=6Sf4Waa";
				//String url="jdbc:postgresql://172.24.2.6:5432/dbbberge15?user=bberge15&password=C3sUFU8";
				
				String url="jdbc:postgresql://localhost:5432/projet_sql?user=postgres&password=aTY68Eg";
				
				
				// url for me
				//String url="jdbc:postgresql://172.24.2.6:5432/dbabashir15?user=abashir15&password=6Sf4Waa";
				
				
				//String url="jdbc:postgresql://localhost:5432/pubs2?user=postgres&password=root";
				//String url="jdbc:postgresql://localhost:5432/pubs2?user=postgres&password=root";
				
				
				conn = null;
				try {
						conn = DriverManager.getConnection(url);
					} catch (SQLException e) {
						System.out.println("Impossible de joindre le server !");
						e.printStackTrace();
						System.exit(1);
					}
				
				try {
					st_ajoutAgent =conn.prepareStatement("SELECT ajoutAgent(?,?,?,?, ?,?)");
					st_listeRisque=conn.prepareStatement("SELECT listeZoneARisque();");
					st_supressionAgent=conn.prepareStatement("select supressionAgent(?)");
					st_historiqueAgent=conn.prepareStatement("SELECT listeHistoriqueAgent(?,?::DATE,?::DATE);");  // HISTORIQUE COMBAT ?
					st_historiqueCombat=conn.prepareStatement("SELECT historiqueCombats(?::DATE,?::DATE);");
					st_classementHeroVictoires=conn.prepareStatement("SELECT * from classementHerosVictoires_view;");
					st_classementHerosDefaites=conn.prepareStatement("SELECT * from classementHerosDefaites_view;");
					st_classementAgents=conn.prepareStatement("SELECT  classementAgents();");
				} catch (SQLException e) {
					e.printStackTrace();
					System.exit(1);
					
				}
				
		
	}
	
	
	
	public static void main(String[] args) {
		Central app = new Central();
		System.out.println("Bienvenu dans l'application principale.");
		while(true){
			System.out.println("Options: 1.Inscrire un agent, 2. utiliser la fonction d'avertissement, 3. Supprimer un agent 4.Afficher les statistiques 5. Afficher l'historique d'un agent 6.Quitter ");
			System.out.println("Veuillez sélectionner un opération à effectuer:  ");
			try {
			int choix = sc.nextInt();
			switch(choix){
			case 1:app.inscrireAgent();break;
			case 2:app.lancerAvertissement();break;
			case 3:app.supprimerAgent();break;
			case 4:app.afficherStatistiques();break;
			case 5: app.afficherHistorique();break;
			case 6:System.exit(0);
			default:System.out.println("erreur");
			
			}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println(e);
			}finally{
				System.out.println("\n mais poursuivons");
			}
		}
	}
	
	private void inscrireAgent() throws SQLException{
		System.out.println("---INSCRIRE UN AGENT---");
		String code_name, nom, prenom, adresse,tel, mdp;
		sc.nextLine();
		System.out.println("Insérez le nom de code:");
		code_name = sc.nextLine();
		System.out.println("Insérez le mot de passe:");
		mdp = sc.nextLine();
		System.out.println("Insérez le nom de l'agent:");
		nom = sc.nextLine();
		System.out.println("Insérez le prenom de l'agent:");
		prenom = sc.nextLine();
		System.out.println("Insérez l'adresse:"); 
		adresse = sc.nextLine();
		System.out.println("Insérez le numéro de téléphone:");
		tel = sc.nextLine();
		
		
		String mdp_hash = BCrypt.hashpw(mdp, BCrypt.gensalt());
		
		
			st_ajoutAgent.setString(1, code_name);
			st_ajoutAgent.setString(2, nom);
			st_ajoutAgent.setString(3, prenom);
			st_ajoutAgent.setString(4, adresse);
			st_ajoutAgent.setString(5, tel);
			st_ajoutAgent.setString(6, mdp_hash);
			st_ajoutAgent.execute();
		
		System.out.println("L'agent a été inscrit");
		System.out.println();
	} 
	private void lancerAvertissement() throws SQLException{
		System.out.println("---AVERTISSEMENTS---");
		
			ResultSet rs= st_listeRisque.executeQuery();
			while(rs.next()) {
			System.out.println(rs.getString(1));
			}
			
			
		
	} 
	private void supprimerAgent() throws SQLException{
		System.out.println("---SUPPRIMERAGENT---");
		String code_name;
		System.out.println("Insérez le nom de code:");
		sc.nextLine();
		code_name = sc.nextLine();
		
			st_supressionAgent.setString(1, code_name);
			st_supressionAgent.execute();
		
		System.out.println("L'agent a été indiqué comme innactif");
		System.out.println();
		
	} 
	private void afficherStatistiques() throws SQLException{
		System.out.println("---STATISTIQUES---");
		System.out.println("Options: 1. voir le classement des Agents 2. voir le classement des Heros par nombre de Defaites 3. voir le classement des Heros par nombre de Victoires 4. Historiques des combats");
		int choix = sc.nextInt();
		switch(choix){
		case 1:classementAgents();break;
		case 2:classementHerosDefaites();break;
		case 3:classementHerosVictoires();break;
		case 4:historiqueDesCombats();break;
		default:System.out.println("Entrée invalide");
		}
		
	} 
	private void historiqueDesCombats() throws SQLException{
		System.out.println("---HISTORIQUE---");
		String date1,date2;
		sc.nextLine();
		System.out.println("Insérez la date de début (yyyy-mm-dd):");
		date1 = sc.nextLine();
		System.out.println("Insérez la date de fin (yyyy-mm-dd):");
		date2 = sc.nextLine();
			st_historiqueCombat.setString(1, date1);
			st_historiqueCombat.setString(2, date2);
			try(ResultSet rs= st_historiqueCombat.executeQuery()){
			while(rs.next()) {
			System.out.println(rs.getString(1));
			}
			}
			
	} 



	private void afficherHistorique() throws SQLException{
		System.out.println("---HISTORIQUE---");
		String date1,date2,nom_agent;
		sc.nextLine();
		System.out.println("Insérez le nom de l'agent:");
		nom_agent = sc.nextLine();
		System.out.println("Insérez la date de début (yyyy-mm-dd):");
		date1 = sc.nextLine();
		System.out.println("Insérez la date de fin (yyyy-mm-dd):");
		date2 = sc.nextLine();
		
			st_historiqueAgent.setString(1, nom_agent);
			st_historiqueAgent.setString(2, date1);
			st_historiqueAgent.setString(3, date2);
			try(ResultSet rs= st_historiqueAgent.executeQuery()){
				while(rs.next()) {
					System.out.println(rs.getString(1));
				}
			}
			
	} 
	
	private  void classementHerosVictoires() throws SQLException{
			ResultSet rs= st_classementHeroVictoires.executeQuery();
			while(rs.next()) {
				System.out.println("id: "+rs.getString(1)+" nom de héro: "+rs.getString(2)+" nombre de victoires: "+rs.getString(3));
			}
			
		
	}
	private void classementHerosDefaites() throws SQLException{
			ResultSet rs= st_classementHerosDefaites.executeQuery();
			while(rs.next()) {
				System.out.println("id: "+rs.getString(1)+" nom de héro: "+rs.getString(2)+" nombre de défates: "+rs.getString(3));
			}
			
			
		
	}
	private void classementAgents() throws SQLException{
		
			ResultSet rs= st_classementAgents.executeQuery();
			while(rs.next()) {
				System.out.println(rs.getString(1));
			}
			
	}
	
}