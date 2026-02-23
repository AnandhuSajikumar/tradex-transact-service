$services = @(
    "tradex-eureka-server",
    "tradex-config-server",
    "tradex-api-gateway",
    "tradex-portfolio-service",
    "tradex-identity-service",
    "tradex-market-service",
    "tradex-transact-service"
)

foreach ($service in $services) {
    Write-Host "========================================"
    Write-Host "Building $service..."
    Write-Host "========================================"
    cd "f:\$service"
    mvn clean install -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Build failed for $service"
        exit $LASTEXITCODE
    }
}
Write-Host "========================================"
Write-Host "All services built successfully!"
Write-Host "========================================"
