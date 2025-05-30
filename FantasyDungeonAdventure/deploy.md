# Quick GitHub Deployment Guide

## 🚀 Get Your Game Live in 5 Minutes!

### Option 1: Web Deployment (Recommended - No Downloads for Players!)

#### Step 1: Push to GitHub

```bash
# Initialize git if not already done
git init
git add .
git commit -m "Initial commit - Fantasy Dungeon Adventure"

# Add your GitHub repository
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
git branch -M main
git push -u origin main
```

#### Step 2: Enable GitHub Pages

1. Go to your repository on GitHub
2. Click **Settings** → **Pages**
3. Under "Source", select **GitHub Actions**
4. **That's it!** The web version will auto-deploy on every push

#### Step 3: Share Your Game

Send people to: `https://YOUR_USERNAME.github.io/YOUR_REPO_NAME/`

**They can play instantly in any browser - no downloads or Java installation required!**

---

### Option 2: Release Downloads

#### Step 1: Push to GitHub (same as above)

#### Step 2: Create Your First Release

```bash
# Create and push a version tag
git tag v1.0.0
git push origin v1.0.0
```

#### Step 3: Share Download Links

Send people to: `https://github.com/YOUR_USERNAME/YOUR_REPO_NAME/releases/latest`

---

## What Players Get

### 🌐 Web Version (Instant Play):
1. Visit your GitHub Pages URL
2. **Option A**: Full Java version (main page) - may take a moment to load
3. **Option B**: HTML5 version (`/simple.html`) - instant, lightweight
4. No installation needed!

### 💾 Download Version:

#### Windows Users:
1. Download `fantasy-dungeon-adventure-windows.zip`
2. Extract the zip file
3. Double-click `run-game.bat` → Game starts!

#### Mac/Linux Users:
1. Download `fantasy-dungeon-adventure-unix.tar.gz`
2. Extract: `tar -xzf fantasy-dungeon-adventure-unix.tar.gz`
3. Run: `./run-game.sh` → Game starts!

#### Any Platform:
- Download the standalone JAR
- Run: `java -jar fantasy-dungeon-adventure-standalone.jar`

---

## Both Deployment Methods Automatically:

### Web Deployment Creates:
- **Full Java Version**: Your actual Java game running in browser via CheerpJ
- **HTML5 Version**: Lightweight, mobile-friendly version
- **Automatic Updates**: Every push to main branch updates the web version

### Release Deployment Creates:
- **Windows Package**: ZIP with batch script for easy running
- **Mac/Linux Package**: TAR.GZ with shell script  
- **Universal JAR**: Works on any platform with Java
- **Detailed Instructions**: Each package includes setup guide

---

## Testing Your Deployment

### Test Web Version Locally:
```bash
# Build and test
mvn clean package

# Test the JAR works
java -jar target/fantasy-dungeon-adventure-1.0-SNAPSHOT-standalone.jar
```

### Create Additional Releases:
```bash
# For updates, just create new tags
git tag v1.1.0
git push origin v1.1.0
```

### Update Web Version:
```bash
# Just push to main branch
git add .
git commit -m "Update game"
git push origin main
# Web version updates automatically!
```

---

## Troubleshooting

**Web version won't load?**
- Check GitHub Actions tab for build errors
- Make sure GitHub Pages is enabled
- Try the HTML5 version at `/simple.html`

**CheerpJ takes too long to load?**
- Direct players to the HTML5 version first
- CheerpJ is powerful but takes 30-60 seconds to initialize

**Build fails?**
- Make sure you have Java 19+ and Maven installed
- Check the GitHub Actions logs

**Game won't start for players?**
- Web version: Try a different browser or the HTML5 version
- Download version: Ensure they have Java 19+ installed
- Direct them to https://adoptium.net/ for Java downloads

**Want to customize?**
- Edit `.github/workflows/web-deploy.yml` for web version changes
- Edit `.github/workflows/build-and-release.yml` for release changes
- Update version numbers in `pom.xml`
- Modify the README.md for better descriptions

---

## 🎯 Pro Tips

### For Maximum Reach:
1. **Start with web version** - gets more players instantly
2. **Mention both options** in your README
3. **HTML5 version** works great on mobile devices
4. **Download version** for power users who want the full experience

### Marketing Your Game:
1. **Social Media**: Share the web link for instant tries
2. **GitHub Topics**: Add relevant tags to your repository
3. **Screenshots**: Add game screenshots to your README
4. **Demo GIF**: Record a short gameplay GIF for the README
5. **Feedback**: Enable GitHub Issues for player feedback

### Keeping Players Engaged:
1. **Updates**: Use semantic versioning (v1.0.0, v1.1.0, v2.0.0)
2. **Changelog**: Document what's new in each release
3. **Analytics**: Monitor GitHub Pages traffic
4. **Community**: Encourage forks and contributions

Your game is now available in multiple formats for maximum accessibility! 🎮

**Web Version**: Instant play for everyone
**Download Version**: Full experience for dedicated players

The future of gaming is multi-platform! 🌟 