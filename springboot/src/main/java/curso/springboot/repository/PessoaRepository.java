package curso.springboot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import curso.springboot.model.Pessoa;

@Repository
@Transactional
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
	@Query("select p from Pessoa p where p.nome like %?1%") //@Query do JPA do Spring, dentro do Query usa JPQL
	List<Pessoa> findPessoaByName(String nome);
	
	@Query("select p from Pessoa p where p.sexopessoa like %?1%")
	List<Pessoa> findPessoaBySexo(String sexo);
	
	@Query("select p from Pessoa p where p.nome like %?1% and p.sexopessoa=?2")
	List<Pessoa> findPessoaByNameSexo(String nome, String sexopessoa);
}
