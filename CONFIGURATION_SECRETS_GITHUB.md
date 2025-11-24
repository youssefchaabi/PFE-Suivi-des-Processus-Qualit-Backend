# 🔐 Configuration des Secrets GitHub pour SonarCloud Backend

## ❌ Problème actuel

L'erreur indique :
```
o '' is not a valid project or module key. It cannot be empty nor contain whitespaces.
```

Cela signifie que les secrets GitHub ne sont **pas configurés** ou sont **vides**.

## ✅ Solution : Configurer les secrets GitHub

### Étape 1 : Aller sur SonarCloud

1. Allez sur https://sonarcloud.io
2. Connectez-vous avec GitHub
3. Sélectionnez le projet **backend**
4. Notez ces 3 informations :
   - **Organization** : `youssefchaabi` (visible en haut)
   - **Project Key** : `youssefchaabi_PFE-Suivi-des-Processus-Qualit-Backend`
   - **Token** : Allez dans **My Account → Security → Generate Token**

### Étape 2 : Créer un token SonarCloud (si pas déjà fait)

1. Sur SonarCloud, cliquez sur votre avatar (en haut à droite)
2. **My Account → Security**
3. **Generate Tokens**
4. Name: `github-backend`
5. Type: **User Token**
6. Cliquez sur **Generate**
7. **COPIEZ LE TOKEN** (commence par `sqp_...`)

### Étape 3 : Ajouter les secrets dans GitHub

1. Allez sur votre repository backend :
   https://github.com/youssefchaabi/PFE-Suivi-des-Processus-Qualit-Backend

2. Cliquez sur **Settings** (en haut)

3. Dans le menu de gauche : **Secrets and variables → Actions**

4. Cliquez sur **New repository secret**

5. Ajoutez ces 3 secrets un par un :

#### Secret 1 : SONAR_TOKEN
- **Name** : `SONAR_TOKEN`
- **Value** : Le token que vous venez de créer (ex: `sqp_abc123...`)
- Cliquez sur **Add secret**

#### Secret 2 : SONAR_PROJECT_KEY
- **Name** : `SONAR_PROJECT_KEY`
- **Value** : `youssefchaabi_PFE-Suivi-des-Processus-Qualit-Backend`
- Cliquez sur **Add secret**

#### Secret 3 : SONAR_ORGANIZATION
- **Name** : `SONAR_ORGANIZATION`
- **Value** : `youssefchaabi`
- Cliquez sur **Add secret**

### Étape 4 : Vérifier les secrets

Vous devriez voir 3 secrets dans la liste :
- ✅ `SONAR_TOKEN`
- ✅ `SONAR_PROJECT_KEY`
- ✅ `SONAR_ORGANIZATION`

### Étape 5 : Relancer le pipeline

```bash
cd suivi-processus-qualite-backend-main
git commit --allow-empty -m "chore: Test avec secrets SonarCloud configurés"
git push origin main
```

## 📊 Résultat attendu

Après configuration des secrets :
- ✅ Le job SonarCloud passera
- ✅ L'analyse sera envoyée à SonarCloud
- ✅ Le Quality Gate sera évalué

## ⚠️ Points importants

1. Les secrets sont **par repository** (pas globaux)
2. Le token doit être un **User Token** (pas un Project Token)
3. La clé du projet doit être **exactement** : `youssefchaabi_PFE-Suivi-des-Processus-Qualit-Backend`
4. L'organisation doit être **exactement** : `youssefchaabi`

## ❓ Si le problème persiste

1. Vérifiez que les secrets sont bien nommés (pas de fautes de frappe)
2. Vérifiez que le token n'a pas expiré
3. Vérifiez que le projet existe bien sur SonarCloud
4. Essayez de régénérer le token

---

**Une fois les secrets configurés, le pipeline fonctionnera parfaitement !** 🎉
