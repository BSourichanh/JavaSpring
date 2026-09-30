package com.bsourichanh.javaspring.service;

/**
 * Contrat de validation des utilisateurs distants (Microservice Users).
 */
public interface UserValidationService {

    /**
     * Interroge GET /users/{id}/valid sur le service utilisateurs.
     * Retourne true si l'utilisateur existe, false dans tous les autres cas (404, erreur réseau...).
     */
    boolean isValidUser(String userId);
}
