# 📊 Guide : Améliorer la couverture de code

## Problème actuel

- **Coverage on New Code : 0.0%** (required ≥ 80%)
- Seulement 2 classes testées : AuthServiceTest et UtilisateurServiceTest

## Solution : Ajouter plus de tests

### Option 1 : Exécuter TOUS les tests existants

Modifiez le pipeline pour exécuter tous les tests au lieu de seulement 2 :

```yaml
# Dans .github/workflows/devops.yml
- name: Exécution des tests unitaires
  run: ./mvnw test
```

Au lieu de :
```yaml
- name: Exécution des tests unitaires
  run: ./mvnw test -Dtest='AuthServiceTest,UtilisateurServiceTest'
```

### Option 2 : Ajouter des tests pour les services principaux

Créez des tests pour :
1. **FicheQualiteService**
2. **FicheSuiviService**
3. **NotificationService**
4. **EmailService**

### Option 3 : Exclure certains packages de la couverture

Ajoutez dans `sonar-project.properties` :

```properties
# Exclure les packages qui n'ont pas besoin de tests
sonar.coverage.exclusions=\
  **/config/**,\
  **/dto/**,\
  **/model/**,\
  **/exception/**,\
  **/util/**,\
  **/controller/**
```

## 🎯 Objectif

- Atteindre **≥ 50%** de couverture sur le nouveau code
- Ou exclure les packages non critiques

## ⚡ Solution RAPIDE

Modifiez le pipeline pour exécuter tous les tests :

1. Ouvrez `.github/workflows/devops.yml`
2. Changez la ligne 36 :
   ```yaml
   run: ./mvnw test
   ```
3. Commit et push
4. Le pipeline exécutera tous les tests disponibles

---

**Temps estimé** : 2 minutes pour modifier le pipeline ⚡
