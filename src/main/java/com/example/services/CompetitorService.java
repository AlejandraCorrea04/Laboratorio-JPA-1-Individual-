package com.example.services;

import com.example.PersistenceManager;
import com.example.models.Competitor;
import com.example.models.CompetitorDTO;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.NotAuthorizedException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import org.codehaus.jettison.json.JSONObject;

@Path("/competitors")
@Produces(MediaType.APPLICATION_JSON)
public class CompetitorService {

    @GET
    @Path("/get")
    public Response getAll() {
        EntityManager entityManager = PersistenceManager.getInstance().getEntityManagerFactory().createEntityManager();
        try {
            Query q = entityManager.createQuery("SELECT u FROM Competitor u ORDER BY u.surname ASC");
            List<Competitor> competitors = q.getResultList();
            return Response.status(200)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(competitors).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).entity("Error interno del servidor").build();
        } finally {
            entityManager.close();
        }
    }

    @POST
    @Path("/add")
    public Response createCompetitor(CompetitorDTO competitor) {
        EntityManager entityManager = PersistenceManager.getInstance().getEntityManagerFactory().createEntityManager();
        JSONObject rta = new JSONObject();

        Competitor competitorTmp = new Competitor();
        competitorTmp.setAddress(competitor.getAddress());
        competitorTmp.setAge(competitor.getAge());
        competitorTmp.setCellphone(competitor.getCellphone());
        competitorTmp.setCity(competitor.getCity());
        competitorTmp.setCountry(competitor.getCountry());
        competitorTmp.setName(competitor.getName());
        competitorTmp.setSurname(competitor.getSurname());
        competitorTmp.setTelephone(competitor.getTelephone());
        competitorTmp.setEmail(competitor.getEmail());

        String hashedPassword = encriptarSHA256(competitor.getPassword());
        competitorTmp.setPassword(hashedPassword);

        try {
            entityManager.getTransaction().begin();
            entityManager.persist(competitorTmp);
            entityManager.getTransaction().commit();
            entityManager.refresh(competitorTmp);
            rta.put("competitor_id", competitorTmp.getId());
        } catch (Throwable t) {
            t.printStackTrace();
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            return Response.status(500).entity("Error al registrar competidor").build();
        } finally {
            entityManager.close();
        }

        return Response.status(200)
                .header("Access-Control-Allow-Origin", "*")
                .entity(rta).build();
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(JSONObject json) {
        EntityManager entityManager = PersistenceManager.getInstance().getEntityManagerFactory().createEntityManager();
        try {
            String email = json.getString("email");
            String password = json.getString("password");
            String hashedPassword = encriptarSHA256(password);

            Query query = entityManager.createQuery(
                    "SELECT c FROM Competitor c WHERE c.email = :email AND c.password = :password"
            );
            query.setParameter("email", email);
            query.setParameter("password", hashedPassword);

            List<Competitor> result = query.getResultList();

            if (result.isEmpty()) {
                return Response.status(401)
                        .header("Access-Control-Allow-Origin", "*")
                        .entity("Email o contraseña inválidos").build();
            }

            Competitor competitor = result.get(0);
            return Response.status(200)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(competitor).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).entity("Error interno del servidor").build();
        } finally {
            entityManager.close();
        }
    }

    private String encriptarSHA256(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
