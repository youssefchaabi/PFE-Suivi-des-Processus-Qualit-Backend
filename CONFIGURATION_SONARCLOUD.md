# 🔧 Configuration SonarCloud - Guide Complet

## ✅ Étape 1: Créer un compte SonarCloud

1. Allez sur https://sonarcloud.io
2. Cliquez sur "Log in" puis "Sign up with GitHub"
3. Autorisez SonarCloud à accéder à votre compte GitHub

## ✅ Étape 2: Créer une organisation

1. Une fois connecté, cliquez sur "+" en haut à droite
2. Sélectionnez "Analyze new project"
3. Choisissez "Create an organization"
4. Sélectionnez votre compte GitHub
5. Choisissez un nom pour votre organisation (ex: `votre-username`)
6. Cliquez sur "Continue"

## ✅ Étape 3: Créer un projet

1. Sélectionnez votre repository `suivi-processus-qualite-backend-main`
2. Cliquez sur "Set Up"
3. Choisissez "With GitHub Actions"
4. SonarCloud va générer automatiquement:
   - **SONAR_TOKEN**: Token d'authentification
   - **Project Key**: Clé unique du projet
   - **Organization**: Nom de votre organisation

## ✅ Étape 4: Configurer les secrets GitHub

Allez dans votre repository GitHub:
1. Settings → Secrets and variables → Actions
2. Cliquez sur "New repository secret"
3. Ajoutez ces 3 secrets:

### Secret 1: SONAR_TOKEN
- **Name**: `SONAR_TOKEN`
- **Value**: Le token généré par SonarCloud (commence par `sqp_...`)

### Secret 2: SONAR_PROJECT_KEY
- **Name**: `SONAR_PROJECT_KEY`
- **Value**: La clé de votre projet (ex: `votre-username_suivi-processus-qualite-backend-main`)

### Secret 3: SONAR_ORGANIZATION
- **Name**: `SONAR_ORGANIZATION`
- **Value**: Le nom de votre organisation (ex: `votre-username`)

## ✅ Étape 5: Mettre à jour le pom.xml

Remplacez les valeurs dans `pom.xml`:

```xml
<properties>
    <sonar.organization>VOTRE-ORGANISATION</sonar.organization>
    <sonar.projectKey>VOTRE-PROJECT-KEY</sonar.projectKey>
</properties>
```

Par exemple:
```xml
<properties>
    <sonar.organization>john-doe</sonar.organization>
    <sonar.projectKey>john-doe_suivi-processus-qualite-backend-main</sonar.projectKey>
</properties>
```

## ✅ Étape 6: Vérifier la configuration

Après avoir configuré les secrets:

1. Faites un commit et push:
```bash
git add .
git commit -m "feat: Configuration SonarCloud"
git push origin main
```

2. Allez dans l'onglet "Actions" de votre repository GitHub
3. Vérifiez que le job "Analyse SonarCloud" s'exécute
4. Une fois terminé, allez sur https://sonarcloud.io
5. Vous devriez voir votre projet avec l'analyse complète

## 📊 Résultat attendu sur SonarCloud

Vous verrez:
- ✅ **Quality Gate**: PASSED
- 📊 **Coverage**: Pourcentage de couverture de code
- 🐛 **Bugs**: 0
- 🔒 **Vulnerabilities**: 0
- 💡 **Code Smells**: Quelques suggestions d'amélioration
- 📈 **Duplications**: Pourcentage de code dupliqué

## 🎯 Commandes utiles

### Analyser localement (optionnel):
```bash
./mvnw clean verify sonar:sonar \
  -Dsonar.projectKey=VOTRE-PROJECT-KEY \
  -Dsonar.organization=VOTRE-ORGANISATION \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.token=VOTRE-TOKEN
```

## ❓ Troubleshooting

### Erreur: "Not authorized"
→ Vérifiez que le `SONAR_TOKEN` est correct dans les secrets GitHub

### Erreur: "Project not found"
→ Vérifiez que `SONAR_PROJECT_KEY` correspond exactement à la clé sur SonarCloud

### Erreur: "Organization not found"
→ Vérifiez que `SONAR_ORGANIZATION` correspond au nom de votre organisation

### Quality Gate Failed
→ C'est normal au début, vous pouvez:
- Ajuster les règles du Quality Gate sur SonarCloud
- Ou améliorer la qualité du code selon les recommandations

## 🎉 Succès!

Une fois configuré, chaque push déclenchera automatiquement:
1. ✅ Build et tests
2. ✅ Analyse SonarCloud
3. ✅ Rapport de qualité visible sur https://sonarcloud.io
4. ✅ Badge de qualité dans votre README

---

**Note**: Les tests d'intégration (`BackendApplicationTests`) sont exclus de l'analyse pour éviter les erreurs MongoDB.
