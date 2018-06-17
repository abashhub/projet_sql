-------------------- drops --------------------

DROP SCHEMA IF EXISTS shyeld CASCADE;
DROP TYPE factions CASCADE;
DROP TYPE resultats CASCADE;

DROP TYPE localisations CASCADE;

DROP TYPE historiqueAgentReturn CASCADE;
DROP TYPE historiqueCombatsReturn CASCADE;
DROP TYPE classementAgentReturn CASCADE;

drop view classementHerosVictoires_view;
drop view classementHerosDefaites_view;


-------------------- create schema + sequences + types --------------------
create schema shyeld;

CREATE SEQUENCE shyeld.pk_agent;
CREATE SEQUENCE shyeld.pk_hero;
CREATE SEQUENCE shyeld.pk_combat;
CREATE SEQUENCE shyeld.pk_reperage;

CREATE TYPE localisations as (coord_x INTEGER, coord_y INTEGER);
CREATE TYPE factions AS ENUM('Marvelle','Décé');
CREATE TYPE resultats AS ENUM('gagnant','perdant','autre');
CREATE TYPE historiqueAgentReturn AS (nom_hero varchar(100), date_ap DATE, localisation localisations);
CREATE TYPE historiqueCombatsReturn AS (id_combat integer, date DATE, nom_hero varchar(100), resultat resultats);
CREATE TYPE classementAgentReturn AS (id_agent integer, code_name varchar(100), reperages bigint);

-------------------- tables --------------------

CREATE TABLE shyeld.agents (
id_agent INTEGER PRIMARY KEY
	DEFAULT NEXTVAL ('shyeld.pk_agent'),
nom VARCHAR(100) NOT NULL CHECK (nom<>''),
prenom VARCHAR(100) NOT NULL CHECK (prenom<>''),
code_name VARCHAR(100)  NOT NULL CHECK (code_name<>''),
adresse VARCHAR(100)NOT NULL CHECK (adresse<>''),
num_tel CHARACTER(10)
	CHECK(num_tel SIMILAR TO '[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]'),
actif boolean NOT NULL DEFAULT (true),
password VARCHAR(100) NOT NULL CHECK (password<>''),
nb_reperages INTEGER
);


CREATE TABLE shyeld.heros(
id_hero INTEGER PRIMARY KEY
	DEFAULT NEXTVAL ('shyeld.pk_hero'),
nom_civil VARCHAR(100) CHECK (nom_civil<>''),
nom_hero VARCHAR(100) CHECK (nom_hero<>''),
adresse VARCHAR(100) CHECK (adresse<>''),
origine VARCHAR(100)  CHECK (origine<>''),
faction factions,
en_vie boolean NOT NULL default (true),
pouvoir VARCHAR(100)  CHECK (pouvoir<>''),
puissance VARCHAR(100)  CHECK (puissance<>''),
victoires INTEGER,
defaites INTEGER
);


CREATE TABLE shyeld.combats(
id_combat INTEGER PRIMARY KEY
	DEFAULT NEXTVAL ('shyeld.pk_combat'),
date DATE NOT NULL,
localisation localisations
);

CREATE TABLE shyeld.participants(
id_combat INTEGER REFERENCES shyeld.combats(id_combat),
id_hero INTEGER REFERENCES shyeld.heros(id_hero),
PRIMARY KEY (id_combat, id_hero),
resultat resultats
);

CREATE TABLE shyeld.reperages(
id_hero INTEGER NOT NULL REFERENCES shyeld.heros(id_hero),
id_agent INTEGER NOT NULL REFERENCES shyeld.agents(id_agent),
id_reperage INTEGER PRIMARY KEY
	DEFAULT NEXTVAL ('shyeld.pk_reperage'),
date DATE NOT NULL,
localisation localisations
);


----------------------------------- GRANT -------------------------------------
GRANT CONNECT ON DATABASE dbbberge15 TO abashir15;
GRANT USAGE ON SCHEMA shyeld TO abashir15; 
GRANT USAGE , SELECT ON ALL SEQUENCES IN SCHEMA shyeld TO abashir15; 
GRANT SELECT ON ALL TABLES IN SCHEMA shyeld TO abashir15;

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE shyeld.participants TO abashir15;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE shyeld.combats TO abashir15;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE shyeld.heros TO abashir15;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE shyeld.agents TO abashir15;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE shyeld.reperages TO abashir15;




-------------------- ajouter agent --------------------

CREATE OR REPLACE FUNCTION ajoutAgent(varchar(100), varchar(100),varchar(100),varchar(100), character(10),  varchar(100)) RETURNS void AS $$
DECLARE
	v_code_name ALIAS FOR $1 ;
	v_nom ALIAS FOR $2 ;
	v_prenom ALIAS FOR $3 ;
	v_adresse ALIAS FOR $4 ;
	v_tel ALIAS FOR $5 ;
	--v_login ALIAS FOR $6;
	v_password ALIAS FOR $6;

BEGIN
	if (select count(a.nom) from shyeld.agents a where true=a.actif and a.code_name=v_code_name) >0 then raise 'Ce nom est déjà porté';
	else
		insert into shyeld.agents (code_name, nom, prenom, adresse, num_tel, password, nb_reperages) values (v_code_name, v_nom, v_prenom, v_adresse, v_tel, v_password,0);
	end if;
RETURN;	
END;
$$ LANGUAGE plpgsql;


-------------------- ajouter hero --------------------

CREATE OR REPLACE FUNCTION ajoutHero(varchar(100), varchar(100),varchar(100),varchar(100), factions, varchar(100), varchar(100)) RETURNS void  AS $$
DECLARE 
	v_nom_civil ALIAS FOR $1;
	v_nom_hero ALIAS FOR $2;
	v_adresse ALIAS FOR $3;
	v_origine ALIAS FOR $4;
	v_faction ALIAS FOR $5;
	v_pouvoir ALIAS FOR $6;
	v_puissance ALIAS FOR $7;
BEGIN
	if (select count(h.nom_hero) from shyeld.heros h where true=ANY(select en_vie from shyeld.heros where nom_hero=v_nom_hero)) >0 then raise 'Ce nom est déjà porté';
	else
		insert into shyeld.heros(nom_civil, nom_hero, adresse, origine, faction, pouvoir, puissance, victoires, defaites) 
			values (v_nom_civil, v_nom_hero, v_adresse, v_origine, v_faction, v_pouvoir, v_puissance, 0, 0);
	RETURN;
	end if;
END;
$$ LANGUAGE plpgsql;



-------------------- supprimer agent --------------------

CREATE OR REPLACE FUNCTION supressionAgent(varchar(100)) returns void AS $$
DECLARE
	v_code_name ALIAS for $1;
	nb_agents_actif integer;
BEGIN
	nb_agents_actif := (select count(id_agent) from shyeld.agents where code_name=v_code_name and actif=true);
	if nb_agents_actif = 0 THEN
		RAISE 'agent innexistant ou déjà inactif';	

	--(select count(id_agent) from shyeld.agents where code_name=v_code_name)==0 THEN 
	else
	UPDATE shyeld.agents
	SET actif = false
	where code_name=v_code_name;

	END IF;
	
END; 
$$ LANGUAGE plpgsql;



-------------------- supprimer hero --------------------

CREATE OR REPLACE FUNCTION supressionHero(varchar(100)) returns void AS $$
DECLARE
	v_nom_hero ALIAS for $1;
	nb_heros_vivant integer;
BEGIN
	nb_heros_vivant := (select count(id_hero) from shyeld.heros where nom_hero=v_nom_hero and en_vie=true);
	if nb_heros_vivant = 0 THEN
		RAISE 'héro innexistant ou déjà mort';	

	--(select count(id_hero) from shyeld.heros where nom_hero=v_nom_hero)==0 THEN 
	else
	UPDATE shyeld.heros
	SET en_vie = false
	where nom_hero=v_nom_hero;

	END IF;
	
	
END; 
$$ LANGUAGE plpgsql;



-------------------- lister heros disparu --------------------



CREATE VIEW heroDisparu_view AS 
SELECT h.nom_hero, a.localisation FROM shyeld.heros h, shyeld.reperages a
WHERE h.id_hero=a.id_hero and a.date >= ALL(select a1.date from shyeld.reperages a1 WHERE a1.id_hero=h.id_hero) 
	and a.date < (CURRENT_DATE-INTERVAL '15 day');


-------------------- ajouter reperage --------------------

CREATE OR REPLACE FUNCTION ajoutReperage(VARCHAR(100), VARCHAR(100),DATE,integer,integer) RETURNS void  AS $$
DECLARE 
	v_nom_hero ALIAS FOR $1;
	v_code_agent ALIAS FOR $2;
	v_date ALIAS FOR $3;
	v_coord_x ALIAS FOR $4;
	v_coord_y ALIAS FOR $5;
	v_id_agent INTEGER;
	v_id_hero INTEGER;
BEGIN
	v_id_hero := (select h.id_hero from shyeld.heros h where h.nom_hero = v_nom_hero and h.en_vie = true);
	v_id_agent := (select a.id_agent from shyeld.agents a where a.code_name = v_code_agent and a.actif=true);
	if (-1) >0 then raise 'exception';
	else
		insert into shyeld.reperages(id_hero, id_agent , date, localisation) 
			values (v_id_hero, v_id_agent , v_date, (v_coord_x,v_coord_y));
	end if;
END;
$$ LANGUAGE plpgsql;


-------------------- ajouter combat --------------------

CREATE OR REPLACE FUNCTION ajoutCombat(integer,integer,DATE) RETURNS integer  AS $$
DECLARE 
	v_date ALIAS FOR $3;
	v_coord_x ALIAS FOR $1;
	v_coord_y ALIAS FOR $2;
	pk integer;
BEGIN
	if (-1) >0 then raise 'exception';
	else
		insert into shyeld.combats(date, localisation) 
			values (v_date, (v_coord_x,v_coord_y))  returning  id_combat  into pk ;
	RETURN pk;
	end if;
END;
$$ LANGUAGE plpgsql;


-------------------- ajouter participant --------------------

CREATE OR REPLACE FUNCTION ajoutParticipants(varchar(100),integer,character varying) RETURNS void  AS $$
DECLARE 
	v_nom_hero ALIAS FOR $1;
	v_id_combat ALIAS FOR $2;
	v_resultat ALIAS FOR $3;
	v_id_hero INTEGER;
BEGIN
	v_id_hero := (select h.id_hero from shyeld.heros h where h.nom_hero = v_nom_hero and h.en_vie = true);
	
	if (v_id_hero) is null then raise 'exception';
	else
		insert into shyeld.participants(id_combat, id_hero, resultat) 
			values (v_id_combat ,v_id_hero, CAST(v_resultat as resultats));
	RETURN;
	end if;
END;
$$ LANGUAGE plpgsql;

-------------------- lister historique agent --------------------

CREATE OR REPLACE FUNCTION listeHistoriqueAgent(varchar(100), DATE, DATE) RETURNS SETOF historiqueAgentReturn AS $$
DECLARE
	v_code_name ALIAS for $1;
	v_date_debut ALIAS for $2;
	v_date_fin ALIAS for $3;
	sortie RECORD;
BEGIN
	if (select count(*) from shyeld.agents where code_name = v_code_name) = 0 THEN RAISE 'agent innexistant';
	END if;
	for sortie in select HE.nom_hero, RE.date, RE.localisation
		from shyeld.agents AG, shyeld.reperages RE, shyeld.heros HE
		where RE.id_agent = AG.id_agent
			AND RE.id_hero = HE.id_hero
			AND AG.code_name = v_code_name
			AND RE.date BETWEEN v_date_debut AND v_date_fin 
	LOOP
		return next sortie;
	END LOOP;
	RETURN;
END;
$$ LANGUAGE 'plpgsql';



-------------------- classement heros _victoires_ --------------------

CREATE VIEW classementHerosVictoires_view AS 
SELECT h.id_hero,h.nom_hero, h.victoires from shyeld.heros h order by h.victoires DESC;


-------------------- trigger heros _victoires_ --------------------

CREATE OR REPLACE FUNCTION total_victoires() RETURNS TRIGGER AS $$
DECLARE
	total_victoires INTEGER;
	total_defaites INTEGER;
BEGIN
	IF NEW.resultat = 'gagnant' THEN
		SELECT HE.victoires from shyeld.heros HE where HE.id_hero = NEW.id_hero INTO total_victoires;
		UPDATE shyeld.heros SET victoires = total_victoires + 1 where id_hero = NEW.id_hero;
	ELSIF NEW.resultat = 'perdant' THEN
		SELECT HE.defaites from shyeld.heros HE where HE.id_hero = NEW.id_hero INTO total_defaites;
		UPDATE shyeld.heros SET defaites = total_defaites + 1 where id_hero = NEW.id_hero;
	END IF;
	
RETURN NULL;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER victoire_defaite_trigger AFTER INSERT ON shyeld.participants FOR EACH ROW EXECUTE PROCEDURE total_victoires();


-------------------- classement heros _defaites_ --------------------

CREATE VIEW classementHerosDefaites_view AS 
SELECT h.id_hero,h.nom_hero, h.defaites from shyeld.heros h order by h.defaites DESC;


-------------------- classement agents --------------------



CREATE OR REPLACE FUNCTION classementAgents() RETURNS SETOF classementAgentReturn AS $$
DECLARE
	agent RECORD;
	sortie RECORD;
BEGIN
	for agent IN select * from shyeld.agents
	LOOP
		for sortie IN select agent.id_agent, agent.code_name, count(AP.*) from shyeld.reperages AP
				where agent.id_agent = AP.id_agent
		LOOP
			return next sortie;
		END LOOP;
	END LOOP;
	
RETURN;
END;
$$ LANGUAGE 'plpgsql';

-------------------- trigger reperages agents--------------------

CREATE OR REPLACE FUNCTION total_reperages() RETURNS TRIGGER AS $$
DECLARE
	total_reperages INTEGER;
BEGIN
	SELECT AG.nb_reperages from shyeld.agents AG where AG.id_agent = NEW.id_agent INTO total_reperages;
	UPDATE shyeld.agents SET nb_reperages = total_reperages + 1 where id_agent = NEW.id_agent;
RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER reperages_trigger AFTER INSERT ON shyeld.reperages FOR EACH ROW EXECUTE PROCEDURE total_reperages();

-------------------- historique combats --------------------

CREATE OR REPLACE FUNCTION historiqueCombats(DATE, DATE) RETURNS SETOF historiqueCombatsReturn AS $$
DECLARE
	v_date_debut ALIAS for $1;
	v_date_fin ALIAS for $2;
	combat RECORD;
	sortie RECORD;
BEGIN
	for combat IN select CO.* from shyeld.combats CO where CO.date BETWEEN v_date_debut AND v_date_fin
	LOOP
		FOR sortie IN select combat.id_combat, combat.date, HE.nom_hero, PA.resultat from shyeld.heros HE, shyeld.participants PA
				where combat.id_combat = PA.id_combat AND PA.id_hero = HE.id_hero
		LOOP
			return next sortie;
		END LOOP;
	END LOOP;
	
RETURN;
END;
$$ LANGUAGE 'plpgsql';

-------------------- lister zone a risque --------------------

---- SI LES HEROS SONT ADGACENTS CONTINUER ICI (adapter pour retourner les 2 localisations) ----
CREATE OR REPLACE FUNCTION listeZoneARisque() RETURNS SETOF localisations AS $$
DECLARE
v_localisations_Décé record;
v_localisations record;

BEGIN
for v_localisations_Décé in select (ap.localisation).coord_x, (ap.localisation).coord_y from shyeld.reperages ap, shyeld.heros h where ap.id_hero = h.id_hero and h.faction = 'Décé' 
loop
for v_localisations in select (ap.localisation).coord_x, (ap.localisation).coord_y from shyeld.reperages ap, shyeld.heros h where ap.id_hero = h.id_hero and h.faction = 'Marvelle' 
and ( ((v_localisations_Décé.coord_x=(ap.localisation).coord_x +1 and v_localisations_Décé.coord_y=(ap.localisation).coord_y)
or (v_localisations_Décé.coord_x=(ap.localisation).coord_x -1 and v_localisations_Décé.coord_y=(ap.localisation).coord_y)
or (v_localisations_Décé.coord_x=(ap.localisation).coord_x and v_localisations_Décé.coord_y=(ap.localisation).coord_y +1 )
or (v_localisations_Décé.coord_x=(ap.localisation).coord_x and v_localisations_Décé.coord_y=(ap.localisation).coord_y -1 )
) ) 
loop
return next (v_localisations_Décé.coord_x, v_localisations_Décé.coord_y ); 
return next (v_localisations.coord_x,v_localisations.coord_y);
end loop;

-- cas où ils sont au même endroit, mis à pars pour ne l'afficher qu'une seule fois
for v_localisations in select (ap.localisation).coord_x, (ap.localisation).coord_y from shyeld.reperages ap, shyeld.heros h where ap.id_hero = h.id_hero and h.faction = 'Marvelle' 
and (v_localisations_Décé.coord_x=(ap.localisation).coord_x and v_localisations_Décé.coord_y=(ap.localisation).coord_y)
loop
return next (v_localisations.coord_x,v_localisations.coord_y);
end loop;
end loop;
RETURN;
END;
$$ LANGUAGE 'plpgsql';





-------------------- modified 5/12/2016 22:52 at home --------------------





select * from shyeld.agents;


select * from shyeld.heros;


select * from shyeld.participants;


select * from shyeld.combats;


select * from shyeld.reperages;



