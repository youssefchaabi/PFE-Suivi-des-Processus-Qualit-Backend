# 🚀 Pipeline CI/CD - Backend

## 📋 Vue d'Ensemble

Le pipeline CI/CD est configuré avec GitHub Actions et s'exécute automatiquement sur les branches `main` et `develop`.

---

## 🔧 Correction Effectuée

### Problème Initial
```
Error: Process completed with exit code 126
/home/runner/work/_temp/3bf9cf31-eb8a-4a31-b04f-7117f686b29.sh: line 1: ./mvnw: Permission denied
```

### Solution Appliquée
Ajout de la commande `chmod +x mvnw` avant chaque utilisation du Maven Wrapper pour donner les permissions d'exécution.

**Avant :**
```yaml
- name: Exécution des tests unitaires
  run: ./mvnw test
```

**Après :**
```yaml
- name: Donner les permissions au Maven Wrapper
  run: chmod +x mvnw

- name: Exécution des tests unitaires
  run: ./mvnw test
```

---

## 🔄 Jobs du Pipeline

### 1. Build et Test Backend
**Déclenchement :** À chaque push ou pull request

**Étapes :**
1. ✅ Checkout du code
2. ✅ Configuration Java 17
3. ✅ Vérification de la version Java
4. ✅ Donner permissions au Maven Wrapper
5. ✅ Build avec Maven (`./mvnw clean install -DskipTests`)
6. ✅ Exécution des tests unitaires (`./mvnw test`)
7. ✅ Génération du rapport de couverture (`./mvnw jacoco:report`)
8. ✅ Upload des artefacts (fichiers JAR)

**Résultat attendu :**
```
Tests run: 23, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

### 2. Analyse de Qualité du Code
**Déclenchement :** Après le build, uniquement sur push

**Étapes :**
1. ✅ Checkout du code
2. ✅ Configuration Java 17
3. ✅ Donner permissions au Maven Wrapper
4. ✅ Analyse SonarQube (si configuré)

**Note :** Continue même en cas d'erreur (`continue-on-error: true`)

---

### 3. Build Docker Image
**Déclenchement :** Après le build, uniquement sur la branche `main`

**Étapes :**
1. ✅ Checkout du code
2. ✅ Configuration Java 17
3. ✅ Donner permissions au Maven Wrapper
4. ✅ Build du projet (`./mvnw clean package -DskipTests`)
5. ✅ Login Docker Hub
6. ✅ Build et Push de l'image Docker

**Tags créés :**
- `latest`
- `{commit-sha}`

---

### 4. Déploiement
**Déclenchement :** Après le build Docker, uniquement sur `main`

**Étapes :**
1. ✅ Checkout du code
2. ✅ Connexion SSH au serveur
3. ✅ Pull de la nouvelle image
4. ✅ Redémarrage du conteneur
5. ✅ Nettoyage des images inutilisées

---

### 5. Notification
**Déclenchement :** Toujours, après tous les jobs

**Étapes :**
- ✅ Notification de succès ou d'échec
- ✅ Affichage de la branche et du commit

---

## 🔑 Secrets Requis

Pour que le pipeline fonctionne complètement, configurez ces secrets dans GitHub :

### Docker Hub
- `DOCKER_USERNAME` - Nom d'utilisateur Docker Hub
- `DOCKER_PASSWORD` - Mot de passe ou token Docker Hub

### SonarQube (optionnel)
- `SONAR_TOKEN` - Token d'authentification SonarQube
- `SONAR_HOST_URL` - URL du serveur SonarQube

### Déploiement (optionnel)
- `SERVER_HOST` - Adresse IP ou domaine du serveur
- `SERVER_USERNAME` - Nom d'utilisateur SSH
- `SERVER_SSH_KEY` - Clé privée SSH
- `SERVER_PORT` - Port SSH (généralement 22)

---

## 📊 Vérification du Pipeline

### Vérifier l'État du Pipeline
1. Aller sur GitHub → Actions
2. Voir l'exécution en cours ou les exécutions passées
3. Cliquer sur un workflow pour voir les détails

### Logs des Tests
```
Build et Test Backend → Exécution des tests unitaires
```

Vous devriez voir :
```
[INFO] Tests run: 23, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 🐛 Dépannage

### Erreur : Permission denied
**Solution :** Ajoutez `chmod +x mvnw` avant d'utiliser `./mvnw`

### Erreur : Tests échouent
**Solution :** Vérifiez que tous les tests passent localement :
```bash
./mvnw test
```

### Erreur : Docker login failed
**Solution :** Vérifiez que les secrets `DOCKER_USERNAME` et `DOCKER_PASSWORD` sont configurés

### Erreur : SonarQube analysis failed
**Solution :** Vérifiez les secrets `SONAR_TOKEN` et `SONAR_HOST_URL`, ou désactivez cette étape

---

## 🎯 Bonnes Pratiques

### 1. Tester Localement Avant de Push
```bash
# Tester le build
./mvnw clean install

# Tester les tests unitaires
./mvnw test

# Tester le package
./mvnw clean package
```

### 2. Utiliser des Branches
- `develop` - Pour le développement
- `main` - Pour la production

### 3. Pull Requests
- Créer une PR de `develop` vers `main`
- Attendre que le pipeline passe
- Merger uniquement si tout est vert ✅

---

## 📈 Métriques du Pipeline

### Temps d'Exécution Moyen
- Build et Test : ~2-3 minutes
- Analyse de Code : ~1-2 minutes
- Build Docker : ~2-3 minutes
- Déploiement : ~1 minute
- **Total : ~6-9 minutes**

### Taux de Réussite Attendu
- ✅ Build : 100%
- ✅ Tests : 100% (23/23 tests)
- ✅ Docker Build : 100%

---

## 🔄 Workflow Complet

```
Push/PR → Build et Test → Analyse Code → Build Docker → Déploiement → Notification
            ✅              ✅              ✅             ✅            ✅
```

---

## 📝 Exemple de Commit Message

Pour déclencher le pipeline :
```bash
git add .
git commit -m "feat: ajout de nouveaux tests unitaires"
git push origin develop
```

Le pipeline se déclenchera automatiquement !

---

## ✅ Checklist Avant de Push

- [ ] Tests passent localement (`./mvnw test`)
- [ ] Build réussit (`./mvnw clean install`)
- [ ] Code formaté correctement
- [ ] Commit message clair
- [ ] Branche à jour avec `main`

---

**Le pipeline est maintenant configuré et prêt à fonctionner ! 🚀**
