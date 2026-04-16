package co.edu.uniquindio.backendpawsoft.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Servicio para manejo de archivos en Cloudinary.
 * Placeholder para implementación futura.
 */
@Service
public class CloudinaryService {

    /**
     * Sube un archivo a Cloudinary.
     * 
     * @param file Archivo a subir
     * @param folder Carpeta en Cloudinary
     * @return URL del archivo subido
     */
    public String uploadFile(MultipartFile file, String folder) {
        // TODO: Implementar integración con Cloudinary
        // Por ahora retornamos una URL de placeholder
        return "https://via.placeholder.com/300x200?text=File+Uploaded";
    }

    /**
     * Elimina un archivo de Cloudinary.
     * 
     * @param fileUrl URL del archivo a eliminar
     */
    public void deleteFile(String fileUrl) {
        // TODO: Implementar eliminación en Cloudinary
        // Por ahora no hace nada
    }
}