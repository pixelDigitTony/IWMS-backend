## Inter‑Warehouse Management System — Backend

**Stack**: Spring Boot (Java 21), Maven, Docker, Render (deploy), PostgreSQL (Supabase), Cloudflare R2 (S3-compatible), JWT (Supabase Auth)

### Goals
- Clean Architecture with clear boundaries: domain, application (use cases), infrastructure, presentation (REST)
- Strong security: JWT validation (Supabase JWKs), RBAC, scoped access per organization/warehouse
- Storage via presigned URLs to R2; database via JDBC to Supabase Postgres

### Functional Features (aligned with the Inter‑Warehouse Stock Monitoring System document)
- Warehouses & Locations
  - Manage organizations, warehouses, zones, and bin/locations
  - Configure capacity, temperature requirements, and warehouse attributes
- Products & Lots
  - SKU catalog with categories, units of measure (UoM), barcodes/SKU codes
  - Batch/Lot and expiry support; serials optional
- Inventory & Monitoring
  - Real‑time stock by warehouse and location; lot‑level balances
  - Stock thresholds: reorder, minimum/maximum; low‑stock/near‑expiry alerts
  - Inventory adjustments (reason‑coded) and cycle counts (blind/controlled)
- Inter‑Warehouse Transfers
  - Request → approve → allocate/pick → pack → dispatch (in‑transit) → receive → reconcile
  - Support partial shipments, shortage/damage reasons, discrepancy reconciliation
- Receiving (Inbound)
  - Against purchase or inter‑warehouse ASN; Goods Receipt Note (GRN) generation
  - Putaway suggestions to zones/bins; quality hold and quarantine
- Shipping (Outbound)
  - Order pick/pack/ship; packing list, labels; carrier and tracking details
- Attachments & Documents
  - Store GRNs, delivery notes, photos, reports in Cloudflare R2 via presigned uploads
- Reporting & Analytics
  - Stock on hand (by warehouse, location, lot), movement history, aging/expiry, adjustment register
- Users, Roles & Access Control
  - Role‑based access with warehouse‑level scoping; approvals by role; audit log of changes and movements
- Audit & Traceability
  - User action audit trail; inventory movement ledger (who, when, where, why)
- Notifications (optional)
  - Low stock and near‑expiry alerts; transfer events (requested, approved, in‑transit, received)

Note: The above list reflects typical IWM/stock monitoring requirements. If any specific items in your document differ, we will refine these features accordingly.

### Folder Architecture (proposed)
```
src/
  main/
    java/com/iwms/
      app/                         # presentation (controllers), DTOs, exception mappers
        controller/
          AuthController.java
          UsersController.java
          WarehousesController.java
          ProductsController.java
          InventoryController.java
          TransfersController.java
          ReceivingController.java
          ShippingController.java
          AttachmentsController.java
          ReportsController.java
        dto/
          auth/
          users/
          warehouses/
          products/
          inventory/
          transfers/
          receiving/
          shipping/
          attachments/
          reports/
        mapper/                    # MapStruct or manual mappers
        advice/                    # @ControllerAdvice, error handling
        config/                    # WebMvcConfig, CORS

      domain/                      # enterprise business rules (entities, value objects)
        auth/
          Role.java
          Privilege.java
          AccessScope.java         # org/warehouse scope
          User.java
        warehouse/
          Warehouse.java
          Zone.java
          Location.java
        product/
          Product.java
          Category.java
          Uom.java
          Lot.java
        inventory/
          InventoryItem.java
          Adjustment.java
          CycleCount.java
        transfer/
          Transfer.java
          TransferStatus.java
          TransferLine.java
        receiving/
          Grn.java
        shipping/
          Shipment.java
        common/
          EntityId.java
          Audit.java

      application/                 # use cases, ports, services (business logic)
        ports/
          AuthPort.java
          UserRepositoryPort.java
          WarehouseRepositoryPort.java
          ProductRepositoryPort.java
          InventoryRepositoryPort.java
          TransferRepositoryPort.java
          AttachmentStoragePort.java     # presigned URL generation
          ReportQueryPort.java
        usecase/
          auth/
            AuthenticateUserUseCase.java
            ResolvePermissionsUseCase.java
          users/
            CreateUserUseCase.java
            AssignRoleUseCase.java
          warehouses/
            CreateWarehouseUseCase.java
          products/
            CreateProductUseCase.java
          inventory/
            AdjustInventoryUseCase.java
            PerformCycleCountUseCase.java
          transfers/
            CreateTransferUseCase.java
            ApproveTransferUseCase.java
            ReceiveTransferUseCase.java
          attachments/
            CreatePresignedUploadUseCase.java
          reports/
            StockOnHandReportUseCase.java
            StockMovementReportUseCase.java
            StockAgingReportUseCase.java
        service/
          RbacService.java         # enforces roles/privileges logic
        common/
          Paging.java
          Result.java

      infrastructure/              # adapters: db, storage, security, external
        persistence/
          jpa/
            entity/                # JPA entities if using ORM
            repository/            # Spring Data adapters → ports
          sql/                     # alternative plain SQL mappers
        security/
          SecurityConfig.java      # resource server, JWKs, method security
          JwtClaimsExtractor.java
          RbacEvaluator.java
        storage/
          r2/
            R2Config.java          # AWS SDK v2 S3 client with endpoint override
            R2StorageAdapter.java  # implements AttachmentStoragePort
        config/
          AppProperties.java
          ObjectMapperConfig.java
          OpenApiConfig.java

      shared/                       # shared helpers
        util/
        constants/

    resources/
      application.yml
      db/migration/                 # Flyway migrations

  test/
    java/...                        # unit and slice tests
```

Barrels: Use package-level barrels via Spring `@Configuration` and modular packages; export public APIs via interfaces in `application.ports.*`.

### Authentication & Authorization
- AuthN: Supabase Auth issues JWTs; backend validates with JWKs:
  - `spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://<project-ref>.supabase.co/auth/v1/keys`
- AuthZ: Role/Privilege model with scope:
  - Suggested roles: `SUPER_ADMIN`, `ORG_ADMIN`, `WAREHOUSE_MANAGER`, `INVENTORY_CONTROLLER`, `OPERATOR`, `VIEWER`, `AUDITOR`.
  - Privileges: users.manage, roles.manage, warehouses.manage, products.manage, inventory.view, adjustments.create, transfers.create, transfers.approve, receiving.process, shipping.process, counts.perform, reports.view, attachments.upload.
  - Scopes: organization and warehouse level; enforced via annotations (e.g., `@PreAuthorize`) + custom `RbacEvaluator`.

Example policy idea
- Transfer approve: requires `transfers.approve` and membership in destination warehouse scope.
- Inventory adjust: requires `adjustments.create` at source warehouse scope.

### Storage (Cloudflare R2)
- Use AWS SDK v2 S3 client with endpoint override and path-style access
- Generate presigned URLs for uploads; clients upload directly to R2
- Virus/MIME validation step optional before marking attachment as accepted

### Database (Supabase Postgres)
- JDBC URL with SSL required
- Flyway migrations for schema versioning
- Consider pgbouncer/pooler settings; tune HikariCP

#### Core Entities (initial)
- Organization, Warehouse, Zone, Location
- Product, Category, UnitOfMeasure, Lot (batch, expiry), optional Serial
- InventoryBalance (warehouse, location, product, lot)
- InventoryAdjustment (reason, qty, refs)
- CycleCount (plan, result lines)
- Transfer (header, lines, status history)
- Receiving (GRN), Shipping (Shipment)
- Attachment (R2 key, metadata)
- User, Role, Privilege, Scope (org/warehouse)

### Configuration (environment)
```
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=${PORT:8080}

# Auth (Supabase)
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI=https://<project-ref>.supabase.co/auth/v1/keys

# Database (Supabase Postgres)
SPRING_DATASOURCE_URL=jdbc:postgresql://db.<project-ref>.supabase.co:5432/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=<secret>

# Cloudflare R2
R2_ENDPOINT=https://<accountid>.r2.cloudflarestorage.com
R2_REGION=auto
R2_ACCESS_KEY=<key>
R2_SECRET_KEY=<secret>
R2_BUCKET=iwms-attachments
```

### API Surface (indicative)
- `POST /auth/login` (optional if you proxy via frontend Supabase) — exchange session → app token if needed
- `GET /users`, `POST /users`, `POST /users/:id/roles`
- `GET/POST /warehouses`, `GET/POST /products`
- `GET /inventory`, `POST /inventory/adjustments`, `POST /inventory/cycle-counts`
- `POST /transfers` → `POST /transfers/:id/approve` → `POST /transfers/:id/receive`
- `POST /receiving`, `POST /shipping`
- `POST /attachments/presign` → returns URL and headers
- `GET /reports/stock-on-hand`, `GET /reports/movement`, `GET /reports/aging`

#### Workflow States (indicative)
- Transfer: DRAFT → REQUESTED → APPROVED → ALLOCATED → PICKED → PACKED → IN_TRANSIT → RECEIVED → RECONCILED
- Receiving: EXPECTED → CHECKED → PUTAWAY_PENDING → COMPLETED
- Shipping: PICK_PENDING → PICKED → PACKED → DISPATCHED → DELIVERED

### Development
```
mvn spring-boot:run
mvn test
```

### Docker & Deployment
- Multi-stage Dockerfile producing a small JRE image
- Render Web Service (Docker) with env vars above; bind to `$PORT`
- Health check: `/actuator/health` (enable Spring Boot Actuator)


