package br.com.lazaro;

import br.com.lazaro.domain.*;
import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class RelacionamentosTest {
    private EntityManagerFactory factory;
    private EntityManager em;

    @BeforeEach
    void preparar() {
        factory = Persistence.createEntityManagerFactory("carrosPU", Map.of(
                "jakarta.persistence.jdbc.url", "jdbc:h2:mem:teste",
                "hibernate.hbm2ddl.auto", "create-drop",
                "hibernate.show_sql", "false"));
        em = factory.createEntityManager();
    }

    @AfterEach
    void fechar() {
        if (em.getTransaction().isActive()) em.getTransaction().rollback();
        em.close();
        factory.close();
    }

    @Test
    void deveCriarTabelasEChavesEstrangeirasAutomaticamente() {
        var tabelas = em.createNativeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES "
                + "WHERE TABLE_SCHEMA = 'PUBLIC' ORDER BY TABLE_NAME").getResultList();
        assertEquals(java.util.List.of("ACESSORIO", "CARRO", "CARRO_ACESSORIO", "MARCA"), tabelas);
        var chaves = em.createNativeQuery("SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS "
                + "WHERE CONSTRAINT_SCHEMA = 'PUBLIC' AND CONSTRAINT_TYPE = 'FOREIGN KEY' "
                + "ORDER BY CONSTRAINT_NAME").getResultList();
        assertEquals(java.util.List.of("FK_CARRO_MARCA", "FK_CA_ACESSORIO", "FK_CA_CARRO"), chaves);
    }

    @Test
    void devePersistirMarcaCompartilhadaEAcessoriosCompartilhados() {
        em.getTransaction().begin();
        Marca marca = new Marca("Toyota");
        Acessorio ar = new Acessorio("Ar-condicionado");
        Acessorio teto = new Acessorio("Teto solar");
        em.persist(marca);
        em.persist(ar);
        em.persist(teto);
        Carro a = new Carro("Corolla", 2025, marca);
        Carro b = new Carro("Corolla Cross", 2026, marca);
        a.adicionarAcessorio(ar);
        a.adicionarAcessorio(ar); // Nao deve duplicar o vinculo.
        a.adicionarAcessorio(teto);
        b.adicionarAcessorio(ar);
        em.persist(a);
        em.persist(b);
        em.getTransaction().commit();
        em.clear();

        Carro salvoA = em.find(Carro.class, a.getId());
        Carro salvoB = em.find(Carro.class, b.getId());
        assertEquals(salvoA.getMarca().getId(), salvoB.getMarca().getId());
        assertEquals(2, salvoA.getAcessorios().size());
        assertEquals(1, salvoB.getAcessorios().size());
        assertTrue(salvoA.getAcessorios().stream().anyMatch(x -> x.getId().equals(ar.getId())));
        assertEquals(ar.getId(), salvoB.getAcessorios().iterator().next().getId());
        assertEquals(3L, ((Number) em.createNativeQuery("select count(*) from carro_acessorio")
                .getSingleResult()).longValue());

        // Excluir um carro nao pode apagar a marca ou os acessorios compartilhados.
        em.getTransaction().begin();
        em.remove(salvoA);
        em.getTransaction().commit();
        em.clear();
        assertNotNull(em.find(Marca.class, marca.getId()));
        assertNotNull(em.find(Acessorio.class, ar.getId()));
        assertEquals(1, em.find(Carro.class, b.getId()).getAcessorios().size());
        assertEquals(1L, ((Number) em.createNativeQuery("select count(*) from carro_acessorio")
                .getSingleResult()).longValue());
    }
}
