Al agregar la anotación @OneToMany en la entidad Competitor, se establece una relación de uno a muchos con la entidad Producto. 
Esta anotación, junto con mappedBy="competitor", indica que la relación está gestionada por el atributo competitor en la clase Producto.
Como resultado, JPA crea una columna de clave foránea competitor_id en la tabla Producto que referencia al id de la tabla Competitor. 
Esto permite que un competidor pueda tener múltiples productos asociados, y la base de datos mantiene esta relación mediante la clave foránea
en la tabla Producto. La anotación cascade=ALL asegura que las operaciones de persistencia, actualización, eliminación, etc., se apliquen 
automáticamente a los productos relacionados.