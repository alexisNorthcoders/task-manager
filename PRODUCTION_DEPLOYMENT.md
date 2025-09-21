# Production Deployment Guide

This guide covers deploying the Task Manager application to production using Docker and PostgreSQL.

## Prerequisites

- Docker and Docker Compose installed on your VPS
- At least 2GB RAM and 10GB disk space
- Domain name (optional, for SSL setup)

## Quick Start

1. **On your VPS, create the deployment directory:**
   ```bash
   mkdir task-manager-production
   cd task-manager-production
   ```

2. **Create docker-compose.yml and .env file** (copy from your repository):
   ```bash
   # Copy the docker-compose.yml file to your VPS
   # Create a .env file with your production values
   ```

3. **Start the services:**
   ```bash
   docker-compose up -d
   ```

4. **Check the status:**
   ```bash
   docker-compose ps
   docker-compose logs taskmanager
   ```

5. **Access the application:**
   - API: http://your-vps-ip:8080
   - GraphQL Playground: http://your-vps-ip:8080/graphiql
   - Health Check: http://your-vps-ip:8080/actuator/health

## Production Configuration

### Security Settings

**IMPORTANT**: Change these default values before deploying to production:

1. **JWT Secret** (in docker-compose.yml):
   ```yaml
   JWT_SECRET: your-super-secret-jwt-key-change-this-in-production
   ```
   Generate a strong secret:
   ```bash
   openssl rand -base64 32
   ```

2. **Database Password** (in docker-compose.yml):
   ```yaml
   POSTGRES_PASSWORD: taskmanager123
   ```

### Environment Variables

Create a `.env` file in your VPS deployment directory with your production values:

```bash
# Database Configuration
POSTGRES_DB=taskmanager
POSTGRES_USER=taskmanager
POSTGRES_PASSWORD=your-secure-db-password-here

# Spring Boot Configuration
SPRING_JPA_DDL_AUTO=update
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/taskmanager
SPRING_DATASOURCE_USERNAME=taskmanager
SPRING_DATASOURCE_PASSWORD=your-secure-db-password-here

# JWT Configuration (CHANGE THESE!)
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production
JWT_EXPIRATION=86400000

# Server Configuration
SERVER_PORT=8080

# Logging
LOGGING_LEVEL=INFO

# File Upload Limits
MAX_FILE_SIZE=10MB
MAX_REQUEST_SIZE=10MB

# Frontend Configuration
PUBLIC_API_URL=http://your-vps-ip:8080
```

The docker-compose.yml is already configured to use these environment variables with sensible defaults.

### Frontend Configuration

The frontend container (`taskmanager-client`) is built using GitHub Actions with the `PUBLIC_API_URL` secret. To fix attachment download issues:

1. **Update the GitHub Secret**:
   - Go to your GitHub repository settings
   - Navigate to Secrets and Variables → Actions
   - Update the `PUBLIC_API_URL` secret to: `http://95.217.177.76:8080`
   - Or use your actual production domain if you have one

2. **Trigger a new build**:
   - Push a commit to the main branch to trigger the GitHub Action
   - Or manually trigger the workflow from the Actions tab

This ensures that:
- Attachment downloads work correctly
- API calls are made to the right server
- Image previews load properly

## SSL/HTTPS Setup (Optional)

### Using Nginx Reverse Proxy

1. **Install Nginx:**
   ```bash
   sudo apt update
   sudo apt install nginx certbot python3-certbot-nginx
   ```

2. **Create Nginx configuration:**
   ```nginx
   server {
       listen 80;
       server_name your-domain.com;
       
       location / {
           proxy_pass http://localhost:8080;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           proxy_set_header X-Forwarded-Proto $scheme;
       }
   }
   ```

3. **Get SSL certificate:**
   ```bash
   sudo certbot --nginx -d your-domain.com
   ```

## Monitoring and Maintenance

### Health Checks

- **Application Health**: http://your-domain:8080/actuator/health
- **Metrics**: http://your-domain:8080/actuator/metrics
- **Prometheus**: http://your-domain:8080/actuator/prometheus

### Database Backup

1. **Create backup:**
   ```bash
   docker-compose exec postgres pg_dump -U taskmanager taskmanager > backup_$(date +%Y%m%d_%H%M%S).sql
   ```

2. **Restore backup:**
   ```bash
   docker-compose exec -T postgres psql -U taskmanager taskmanager < backup_file.sql
   ```

### Logs

```bash
# View all logs
docker-compose logs

# View specific service logs
docker-compose logs taskmanager
docker-compose logs postgres

# Follow logs in real-time
docker-compose logs -f taskmanager
```

### Updates

1. **Pull latest image and restart:**
   ```bash
   docker-compose pull taskmanager
   docker-compose up -d
   ```

2. **Or restart with latest image:**
   ```bash
   docker-compose down
   docker-compose up -d
   ```

3. **Update frontend with new API URL:**
   ```bash
   # If you need to change the API URL:
   # 1. Update the PUBLIC_API_URL secret in GitHub
   # 2. Push a commit to trigger the GitHub Action
   # 3. Pull the new image
   docker-compose pull taskmanager-client
   docker-compose up -d taskmanager-client
   ```

## Troubleshooting

### Common Issues

1. **Application won't start:**
   ```bash
   # Check logs
   docker-compose logs taskmanager
   
   # Check if database is ready
   docker-compose logs postgres
   ```

2. **Database connection issues:**
   ```bash
   # Check if PostgreSQL is running
   docker-compose exec postgres pg_isready -U taskmanager
   
   # Check database logs
   docker-compose logs postgres
   ```

3. **Port conflicts:**
   ```bash
   # Check what's using port 8080
   sudo netstat -tlnp | grep :8080
   
   # Change port in docker-compose.yml
   ports:
     - "8081:8080"  # Use port 8081 instead
   ```

4. **Attachment downloads not working:**
   ```bash
   # Check if the GitHub secret is set correctly
   # Go to GitHub → Settings → Secrets and Variables → Actions
   # Verify PUBLIC_API_URL secret is set to: http://95.217.177.76:8080
   
   # Pull the latest image (after updating the secret and triggering a build)
   docker-compose pull taskmanager-client
   docker-compose up -d taskmanager-client
   
   # Verify the API is accessible from the frontend container
   docker-compose exec taskmanager-client curl -I http://95.217.177.76:8080/actuator/health
   
   # Check browser console for connection errors
   # Should see requests to http://95.217.177.76:8080/api/attachments/download/X
   # NOT http://localhost:8080/api/attachments/download/X
   ```

### Performance Tuning

1. **Database optimization:**
   - Increase PostgreSQL shared_buffers
   - Configure connection pooling
   - Add database indexes for frequently queried fields

2. **Application optimization:**
   - Configure JVM memory settings
   - Enable connection pooling
   - Set up caching

## Default Users

The application creates default users on startup:
- **Admin**: username: `admin`, password: `admin123`
- **User**: username: `user`, password: `user123`

**Change these passwords immediately in production!**

## File Structure

**On your VPS:**
```
task-manager-production/
├── docker-compose.yml          # Production deployment
├── .env                       # Environment variables (create this)
└── uploads/                   # File uploads (mounted as volume)
```

**In your repository (for GitHub Actions):**
```
task-manager/
├── Dockerfile                  # Multi-stage build
├── .dockerignore              # Docker build optimization
├── docker-compose.yml         # Production deployment template
└── src/                       # Application source code
```

## Support

For issues or questions:
1. Check the logs: `docker-compose logs`
2. Verify health endpoints: `/actuator/health`
3. Check database connectivity: `docker-compose exec postgres psql -U taskmanager -d taskmanager`
