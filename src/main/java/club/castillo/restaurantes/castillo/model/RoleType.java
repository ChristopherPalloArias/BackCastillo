package club.castillo.restaurantes.castillo.model;

public class RoleType {
    public static final String OWNER = "OWNER";
    public static final String RESTAURANT_ADMIN = "RESTAURANT_ADMIN";
    public static final String CUSTOMER = "CUSTOMER";
    public static final String INVITED = "INVITED"; // <--- agrega este rol

    private RoleType() {
        // Constructor privado para evitar instanciación
    }
} 