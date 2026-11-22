# ConexionTester

Probador de conexión JDBC con interfaz Swing (Java, sin frameworks externos).

## Cómo ejecutarlo en Visual Studio Code

1. Instale el JDK 17 o 21 y la extensión **Extension Pack for Java**.
2. Descargue `sqlite-jdbc-3.36.0.3.jar` (Maven Central, `org.xerial:sqlite-jdbc`) y cópielo en la carpeta `lib/`.
3. Abra esta carpeta con **File > Open Folder**.
4. Abra `src/ConexionTester.java` y pulse **Run** sobre el método `main`.

## Pruebas sugeridas

| Prueba | URL | Resultado esperado |
|---|---|---|
| Éxito (verde) | `jdbc:sqlite:inventario.db` | Conexión exitosa, crea `inventario.db` |
| Fallo de conexión (rojo) | `jdbc:sqlite:C:/no/existe/x.db` | SQLException con mensaje técnico |
| Fallo de driver (rojo) | Ejecutar sin el jar | ClassNotFoundException |
| URL sin driver (rojo) | `jdbc:foo:inventario.db` | No suitable driver found |

Usuario y contraseña van vacíos para SQLite.
