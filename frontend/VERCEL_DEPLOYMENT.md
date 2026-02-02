# Deploying to Vercel

This guide will help you deploy the Sequence Memory CAPTCHA frontend to Vercel.

## Prerequisites

1. A Vercel account ([sign up here](https://vercel.com/signup))
2. Your backend API deployed and accessible (or running locally for testing)
3. Git repository with your code

## Quick Deploy

### Option 1: Deploy via Vercel Dashboard (Recommended)

1. **Push your code to GitHub/GitLab/Bitbucket**
   ```bash
   git add .
   git commit -m "Ready for Vercel deployment"
   git push origin main
   ```

2. **Import Project on Vercel**
   - Go to [vercel.com/new](https://vercel.com/new)
   - Click "Import Git Repository"
   - Select your repository
   - Choose the `frontend` folder as the root directory

3. **Configure Project Settings**
   - **Framework Preset**: Vite (auto-detected)
   - **Root Directory**: `frontend`
   - **Build Command**: `npm run build` (auto-detected)
   - **Output Directory**: `dist` (auto-detected)
   - **Install Command**: `npm install` (auto-detected)

4. **Set Environment Variables**
   - Go to Project Settings → Environment Variables
   - Add:
     ```
     VITE_API_URL=https://your-backend-domain.com/api/v1
     ```
   - Replace with your actual backend URL

5. **Deploy**
   - Click "Deploy"
   - Wait for build to complete
   - Your app will be live!

### Option 2: Deploy via Vercel CLI

1. **Install Vercel CLI**
   ```bash
   npm i -g vercel
   ```

2. **Login to Vercel**
   ```bash
   vercel login
   ```

3. **Navigate to frontend directory**
   ```bash
   cd frontend
   ```

4. **Deploy**
   ```bash
   vercel
   ```
   
   Follow the prompts:
   - Set up and deploy? **Yes**
   - Which scope? (Select your account)
   - Link to existing project? **No** (first time) or **Yes** (subsequent)
   - Project name: `ml-captcha-frontend` (or your choice)
   - Directory: `./` (current directory)
   - Override settings? **No**

5. **Set Environment Variables**
   ```bash
   vercel env add VITE_API_URL
   ```
   - Enter your backend API URL when prompted
   - Select environments: Production, Preview, Development

6. **Deploy to Production**
   ```bash
   vercel --prod
   ```

## Environment Variables

### Required

- `VITE_API_URL` - Your backend API URL
  - Example: `https://api.yourdomain.com/api/v1`
  - Or: `http://localhost:8080/api/v1` (for local testing)

### Setting in Vercel Dashboard

1. Go to your project on Vercel
2. Click **Settings** → **Environment Variables**
3. Add variable:
   - **Key**: `VITE_API_URL`
   - **Value**: Your backend API URL
   - **Environments**: Select Production, Preview, Development
4. Click **Save**

## Project Configuration

The `vercel.json` file is already configured with:
- Build command: `npm run build`
- Output directory: `dist`
- Framework: Vite
- SPA routing support (all routes redirect to index.html)

## Backend CORS Configuration

Make sure your backend allows requests from your Vercel domain:

**Backend `application.yml` or `SecurityConfig.java`:**
```yaml
# Add your Vercel domain to allowed origins
spring:
  cors:
    allowed-origins:
      - https://your-app.vercel.app
      - https://your-custom-domain.com
```

Or in `SecurityConfig.java`:
```java
configuration.setAllowedOrigins(Arrays.asList(
    "https://your-app.vercel.app",
    "https://your-custom-domain.com"
));
```

## Custom Domain (Optional)

1. Go to Project Settings → Domains
2. Add your custom domain
3. Follow DNS configuration instructions
4. Vercel will automatically provision SSL certificate

## Continuous Deployment

Vercel automatically deploys on every push to:
- **Production**: `main` or `master` branch
- **Preview**: All other branches and pull requests

## Troubleshooting

### Build Fails

1. **Check build logs** in Vercel dashboard
2. **Verify Node.js version**: Vercel uses Node 18.x by default
3. **Check dependencies**: Ensure all packages are in `package.json`

### API Calls Fail

1. **Verify `VITE_API_URL`** is set correctly
2. **Check CORS** settings on backend
3. **Verify backend is accessible** from internet
4. **Check browser console** for errors

### Routing Issues (404 on refresh)

The `vercel.json` already includes SPA routing support. If issues persist:
- Verify `vercel.json` is in the root directory
- Check that rewrites are configured correctly

### Environment Variables Not Working

1. **Redeploy** after adding environment variables
2. **Verify variable name** starts with `VITE_` (required for Vite)
3. **Check environment** (Production vs Preview)

## Testing Locally Before Deploy

```bash
# Build for production
npm run build

# Preview production build
npm run preview

# Test with production API URL
VITE_API_URL=https://your-api.com/api/v1 npm run preview
```

## Example Deployment URLs

After deployment, you'll get:
- **Production**: `https://ml-captcha-frontend.vercel.app`
- **Preview**: `https://ml-captcha-frontend-git-branch.vercel.app`
- **Custom**: `https://your-custom-domain.com`

## Next Steps

1. ✅ Deploy backend API (separate service)
2. ✅ Set `VITE_API_URL` environment variable
3. ✅ Configure CORS on backend
4. ✅ Test the deployed frontend
5. ✅ Set up custom domain (optional)

## Support

- [Vercel Documentation](https://vercel.com/docs)
- [Vite Deployment Guide](https://vitejs.dev/guide/static-deploy.html#vercel)
- [Vercel Support](https://vercel.com/support)
