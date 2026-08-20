# Dtunnel Manager Oficial

Base aislada para desarrollar el administrador Dtunnel. Incluye una aplicación Android Kotlin/Jetpack Compose con perfiles locales, dashboard, configuración, logs y servicios de túnel.

## Alcance actual
- Desarrollo local y seguro; no se conecta automáticamente a ninguna VPS.
- Los perfiles de servidor se almacenan localmente.
- La conexión SSH y la administración remota se agregarán después de una auditoría de seguridad.
- No ejecutar instaladores de terceros contra servidores productivos.

## Compilación
La compilación de prueba se ejecuta mediante GitHub Actions con `assembleDebug`.
