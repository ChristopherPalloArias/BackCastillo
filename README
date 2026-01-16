
---

## 👨‍💻 Autor

**Christopher**  
Desarrollador | Spring Boot & Java

---

## 🎯 Propósito del Proyecto

BackCastillo es un sistema backend robusto diseñado para:

- ✅ Gestionar operaciones de restaurantes (menús, pedidos, reservas)
- ✅ Autenticación y autorización segura con **JWT**
- ✅ Validación integral de datos con Spring Validation
- ✅ Comunicación en tiempo real mediante **WebSockets**
- ✅ Documentación automática con **Swagger/OpenAPI**
- ✅ Persistencia de datos en **PostgreSQL**
- ✅ Cobertura de pruebas con **JUnit 5 + Mockito**

---

## 🏢 A Qué Empresa Va Dirigido

**BackCastillo** está orientado a:

| Tipo de Negocio | Aplicación |
|-----------------|-----------|
| 🍴 **Cadenas de Restaurantes** | Gestión centralizada de múltiples sucursales |
| 🏪 **Restaurantes Independientes** | Control de operaciones diarias |
| 📱 **Aplicaciones Móviles** | Backend REST para apps de clientes |
| 🔄 **Sistemas de Delivery** | Integración con plataformas de pedidos |
| 📊 **Soluciones Empresariales** | APIs escalables para POS y ERP |

---

## 🛠️ Stack Tecnológico

### Backend Framework
| Componente | Versión | Propósito |
|-----------|---------|----------|
| **Spring Boot** | 3.5.3 | Framework principal |
| **Spring Data JPA** | Latest | ORM y persistencia |
| **Spring Security** | Latest | Autenticación y autorización |
| **Spring Validation** | Latest | Validación de datos |
| **Spring WebSocket** | Latest | Comunicación bidireccional |

### Seguridad & Autenticación
| Herramienta | Versión | Función |
|------------|---------|---------|
| **JWT (JJWT)** | 0.11.5 | Tokens seguros sin estado |
| **Spring Security** | Latest | Control de acceso |

### Base de Datos
| Tecnología | Versión | Uso |
|-----------|---------|-----|
| **PostgreSQL** | 15+ | Base de datos relacional |
| **Hibernate** | Latest | Mapeo objeto-relacional |

### Documentación & Testing
| Herramienta | Versión | Descripción |
|------------|---------|-----------|
| **Springdoc OpenAPI** | 2.1.0 | Documentación Swagger automática |
| **JUnit 5** | 5.8.2 | Framework de testing unitario |
| **Mockito** | 5.2.0 | Mocking para tests |
| **Spring Test** | Latest | Testing integrado |

### Utilidades
| Librería | Versión | Propósito |
|----------|---------|----------|
| **Lombok** | Latest | Reducción de boilerplate |
| **dotenv-java** | 3.0.0 | Gestión de variables de entorno |
| **Commons Codec** | Latest | Codificación (Base64, etc.) |

---

## 📋 Estructura del Proyecto

```
BackCastillo/
├── src/
│   ├── main/
│   │   ├── java/club/castillo/restaurantes/castillo/
│   │   │   ├── CastilloApplication.java       # Clase principal
│   │   │   ├── config/                        # Configuraciones (Security, JWT, WebSocket)
│   │   │   ├── controller/                    # Controladores REST
│   │   │   ├── dto/                           # Data Transfer Objects
│   │   │   ├── model/                         # Entidades JPA
│   │   │   ├── repository/                    # Interfaces de persistencia
│   │   │   ├── service/                       # Lógica de negocio
│   │   │   └── security/                      # Componentes de seguridad
│   │   └── resources/
│   │       ├── application.properties         # Configuración producción
│   │       ├── application.yml                # Configuración YAML
│   │       └── banner.txt                     # Banner de inicio
│   └── test/
│       └── java/club/castillo/restaurantes/castillo/
│           └── (Clases de prueba)
├── .mvn/wrapper/                              # Maven Wrapper
├── pom.xml                                    # Dependencias Maven
├── Dockerfile                                 # Containerización
├── mvnw & mvnw.cmd                            # Scripts de Maven
├── .env                                       # Variables de entorno
└── README                                     # Este archivo
```

---

## 🚀 Características Principales

### 🔐 Autenticación JWT
- Tokens JWT sin estado
- Refresh token para renovación segura
- Validación automática en endpoints protegidos

### 📡 APIs REST Documentadas
- Documentación automática con **Swagger UI**
- Especificación OpenAPI 3.0
- Accesible en: `http://localhost:8080/swagger-ui.html`

### ⚡ WebSockets
- Comunicación en tiempo real
- Notificaciones de pedidos y reservas en vivo
- Escalable a múltiples clientes simultáneos

### ✅ Validación Integral
- Anotaciones de Spring Validation
- Mensajes de error personalizados
- Validación en capas (DTO → Entity)

### 🗄️ Persistencia PostgreSQL
- Esquema relacional optimizado
- Transacciones ACID garantizadas
- Migraciones versionadas

### 🧪 Testing Completo
- Tests unitarios con JUnit 5
- Mocking con Mockito
- Tests de integración con Spring Test

---

## 📦 Instalación y Configuración

### Requisitos Previos
- **Java 17** o superior
- **PostgreSQL 13+**
- **Maven 3.8+** (incluido con mvnw)
- **Git**

### 1️⃣ Clonar el Repositorio
```bash
git clone https://github.com/ChristopherPalloArias/BackCastillo.git
cd BackCastillo
```

### 2️⃣ Configurar Variables de Entorno
Editar el archivo .env:

```properties
# Base de datos
DB_IP=localhost
DB_PORT=5432
DB_DATABASE=castillo_db
DB_USER=admin
DB_PASSWORD=tu_contraseña_segura

# Seguridad
SECRET_KEY_JWT=tu_clave_secreta_64_caracteres
```

### 4️⃣ Ejecutar la Aplicación

**Con Maven Wrapper (Linux/Mac):**
````bash
./mvnw spring-boot:run
````

**Con Maven Wrapper (Windows):**
````cmd
mvnw.cmd spring-boot:run
````

**O compilar y ejecutar:**
````bash
./mvnw clean package
java -jar target/castillo-0.0.1-SNAPSHOT.jar
````

✅ La aplicación iniciará en: **http://localhost:8080**

---

## 📚 Documentación de la API

### Acceso a Swagger
```
URL: http://localhost:8080/swagger-ui.html
```

### Ejemplo de Endpoints (Estructura esperada)

| Método | Endpoint | Descripción |
|--------|----------|------------|
| `POST` | `/api/auth/login` | Autenticación con JWT |
| `POST` | `/api/auth/refresh` | Renovar token JWT |
| `GET` | `/api/restaurants` | Listar restaurantes |
| `POST` | `/api/restaurants` | Crear restaurante |
| `GET` | `/api/menus/{id}` | Obtener menú |
| `POST` | `/api/orders` | Crear pedido |
| `WS` | `/ws/notifications` | WebSocket de notificaciones |

---

## 🧪 Testing

### Ejecutar Tests Unitarios
````bash
./mvnw test
````

### Ejecutar Tests con Cobertura
````bash
./mvnw test jacoco:report
# Reporte en: target/site/jacoco/index.html
````

### Estructura de Tests
- **Unit Tests**: Lógica de servicios con Mockito
- **Integration Tests**: APIs REST completas
- **Security Tests**: Autenticación y autorización

---

## 🐳 Containerización con Docker

### Construir Imagen Docker
````bash
docker build -t backcastillo:1.0 .
````

### Ejecutar Contenedor
````bash
docker run -p 8080:8080 \
  -e DB_IP=tu_db_ip \
  -e DB_PORT=5432 \
  -e DB_DATABASE=castillo_db \
  -e DB_USER=admin \
  -e DB_PASSWORD=tu_password \
  -e SECRET_KEY_JWT=tu_secret \
  backcastillo:1.0
````

### Docker Compose (Recomendado)
Crear `docker-compose.yml`:

````yaml
version: '3.8'
services:
  db:
    image: postgres:15
    environment:
      POSTGRES_DB: castillo_db
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: tu_password
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      DB_IP: db
      DB_PORT: 5432
      DB_DATABASE: castillo_db
      DB_USER: admin
      DB_PASSWORD: tu_password
      SECRET_KEY_JWT: tu_secret
    depends_on:
      - db

volumes:
  pgdata:
````

Ejecutar:
````bash
docker-compose up -d
````

## 🔄 CI/CD con GitHub Actions

Crear archivo `.github/workflows/ci.yml`:

````yaml
name: CI/CD Pipeline

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up Java
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run tests
        run: ./mvnw clean test
      - name: Build package
        run: ./mvnw clean package
````

---

## 📜 Licencia

Este proyecto está bajo licencia **MIT**. Siéntete libre de usarlo en tus propios proyectos.

---


## 📞 Contacto y Soporte

- **Desarrollador**: Christopher
- **Email**: christopherpallo2000@gmail.com

---

## 🎓 Tecnologías Aprendidas

Este proyecto demonstra:
- ✅ Arquitectura Clean Code con Spring Boot
- ✅ Autenticación stateless con JWT
- ✅ Validación de datos integral
- ✅ Testing unitario e integración
- ✅ Documentación automática con OpenAPI
- ✅ Comunicación en tiempo real (WebSockets)
- ✅ Containerización con Docker
- ✅ CI/CD con GitHub Actions

---
