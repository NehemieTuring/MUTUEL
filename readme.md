Render :    testmutuelle4@gmail.com Mutuelle4test@  681050702
Gmail : mutuelle mobile   esnpy   01-01-2000 mutuelleenspy@gmail.com:Mutuelle4test@

spring.mail.username=alongamofranky@gmail.com
spring.mail.password=qhjc neen rwpc kzwq

commande pour lancer en local avec Mysql :
mvn spring-boot:run "-Dspring.profiles.active=dev"

docker compose up --build -d





#gandal
sudo -u postgres psql <<EOF
-- Supprimer les anciens
DROP DATABASE mutuelle_mobile;
DROP USER mutuelle_user;

-- Créer les nouveaux
CREATE USER mutuelle_user1 WITH PASSWORD 'root';
CREATE DATABASE mutuelle_mobile1 OWNER mutuelle_user1;
GRANT ALL PRIVILEGES ON DATABASE mutuelle_mobile1 TO mutuelle_user1;
EOF
 


# 4. Autoriser les connexions réseau
PG_VERSION=$(ls /etc/postgresql/)
PG_HBA="/etc/postgresql/$PG_VERSION/main/pg_hba.conf"
PG_CONF="/etc/postgresql/$PG_VERSION/main/postgresql.conf"

# Ecouter sur toutes les interfaces
sed -i "s/#listen_addresses = 'localhost'/listen_addresses = '*'/" $PG_CONF

# Autoriser le réseau local
echo "host    mutuelle_mobile1    mutuelle_user1    10.50.30.0/24    md5" >> $PG_HBA

# 5. Redémarrer PostgreSQL
systemctl restart postgresql

# 6. Vérification
systemctl status postgresql --no-pager
echo "---"
sudo -u postgres psql -c "\l" | grep mutuelle_mobile1













#back gandal 
# 1. Nettoyer l'ancien dépôt Docker mal ajouté
rm -f /etc/apt/sources.list.d/docker.list
rm -f /etc/apt/keyrings/docker.gpg

# 2. Installer les prérequis
apt-get install -y ca-certificates curl

# 3. Ajouter la clé GPG Docker pour Debian
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc
chmod a+r /etc/apt/keyrings/docker.asc

# 4. Ajouter le dépôt Docker Debian
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

# 5. Mettre à jour et installer Docker
apt-get update
apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# 6. Vérifier
docker --version
docker compose version



