package com.iftm.client.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iftm.client.entities.Client;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    @Query("SELECT c FROM Client c WHERE c.id = :id")
    public Optional<Client> findById(Long id);

    /*
    @Modifying
    @Query("DELETE FROM Client c WHERE c.id = :id")
    public void deleteById(Long id);
     */

    @Modifying
    @Query("DELETE FROM Client obj WHERE " + "obj.cpf = :cpf")
    void deleteClientByCPF(String cpf);

    @Query("SELECT DISTINCT obj FROM Client obj WHERE obj.cpf = :cpf")
    Optional<Client> findClientByCPf(String cpf);

    public List<Client> findByCpfStartingWith(String cpf);
    @Query("SELECT c FROM Client c WHERE LOWER(c.name) = LOWER(:name)")
    Client findByNameIgnoreCase(@Param("name") String name);

    @Query("SELECT c FROM Client c WHERE LOWER(c.name) LIKE %:name%")
    //@Query("SELECT c FROM Client c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Client> findByNames(@Param("name") String name);

    @Query("SELECT c FROM Client c WHERE c.income > :income")
    List<Client> findByIncomeGreaterThan(@Param("income") Double income);

    @Query("SELECT c FROM Client c WHERE c.income < :income")
    List<Client> findByIncomeLessThan(@Param("income") Double income);

    @Query("SELECT c FROM Client c WHERE c.income >= :minimum AND c.income <= :maximum")
    List<Client> findByIncomeByValueRange(@Param("minimum") Double minimum, @Param("maximum") Double maximum);

    List<Client> findClientByBirthDateBetween(Instant DataInicio, Instant DataTermino);

    @Query("SELECT c FROM Client c WHERE c.birthDate BETWEEN :dataInicio AND :dataTerminio")
    List<Client> findClientByBirthDateBetweenQuery(@Param("dataInicio") Instant dataInicio, @Param("dataTerminio") Instant dataTermino);

    Page<Client> findByIncomeGreaterThan(double salarioI, Pageable pageable);

    Page<Client> findByCpfLike(String parteCpf, Pageable pageable);

}


