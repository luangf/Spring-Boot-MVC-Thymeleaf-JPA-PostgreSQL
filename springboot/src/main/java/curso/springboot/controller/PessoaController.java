package curso.springboot.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import curso.springboot.model.Pessoa;
import curso.springboot.model.Telefone;
import curso.springboot.repository.PessoaRepository;
import curso.springboot.repository.TelefoneRepository;

@Controller
public class PessoaController {

	@Autowired
	private PessoaRepository pessoaRepository;

	@Autowired
	private TelefoneRepository telefoneRepository;
	
	@RequestMapping(method = RequestMethod.GET, value="/cadastropessoa")
	public ModelAndView inicio() {
		ModelAndView modelAndView=new ModelAndView("cadastro/cadastropessoa");
		Iterable<Pessoa> pessoasIt=pessoaRepository.findAll();
		modelAndView.addObject("pessoas", pessoasIt);
		modelAndView.addObject("pessoaObj", new Pessoa());
		return modelAndView;
	}
	
	@RequestMapping(method = RequestMethod.POST, value="**/salvarpessoa")
	public ModelAndView salvar(@Valid Pessoa pessoa, BindingResult bindingResult) {
		pessoa.setTelefones(telefoneRepository.getTelefones(pessoa.getId()));
		
		if(bindingResult.hasErrors()) {
			ModelAndView modelAndView=new ModelAndView("cadastro/cadastropessoa");
			Iterable<Pessoa> pessoasIt=pessoaRepository.findAll();
			modelAndView.addObject("pessoas", pessoasIt);
			modelAndView.addObject("pessoaObj", pessoa);
			
			List<String> msg=new ArrayList<String>();
			for (ObjectError objectError : bindingResult.getAllErrors()) {
				msg.add(objectError.getDefaultMessage());
			}
			modelAndView.addObject("msg", msg);
			
			return modelAndView;
		}
		
		pessoaRepository.save(pessoa);
		
		ModelAndView modelAndView=new ModelAndView("cadastro/cadastropessoa");
		Iterable<Pessoa> pessoasIt=pessoaRepository.findAll();
		
		//o thymeleaf carrega todas as pessoas na tabela através desse atributo
		modelAndView.addObject("pessoas", pessoasIt);
		//limpa o form depois de salvar, o thymeleaf pega o atributo
		modelAndView.addObject("pessoaObj", new Pessoa());
		
		return modelAndView;
	}
	
	@RequestMapping(method = RequestMethod.GET, value="/listapessoas")
	public ModelAndView pessoas() {
		Iterable<Pessoa> pessoasIt=pessoaRepository.findAll();
		
		ModelAndView modelAndView=new ModelAndView("cadastro/cadastropessoa");
		modelAndView.addObject("pessoas", pessoasIt);
		modelAndView.addObject("pessoaObj", new Pessoa());
		
		return modelAndView;
	}
	
	@GetMapping("/editarpessoa/{idpessoa}")
	public ModelAndView editar(@PathVariable(value = "idpessoa") Long idpessoa) {
		Optional<Pessoa> pessoa=pessoaRepository.findById(idpessoa);
		
		ModelAndView modelAndView=new ModelAndView("cadastro/cadastropessoa");
		modelAndView.addObject("pessoaObj", pessoa.get());
		
		Iterable<Pessoa> pessoasIt=pessoaRepository.findAll();
		modelAndView.addObject("pessoas", pessoasIt);
		
		return modelAndView;
	}
	
	@GetMapping("/removerpessoa/{idpessoa}")
	public ModelAndView excluir(@PathVariable(value = "idpessoa") Long idpessoa) {
		pessoaRepository.deleteById(idpessoa);
		
		ModelAndView modelAndView=new ModelAndView("cadastro/cadastropessoa");
		modelAndView.addObject("pessoas", pessoaRepository.findAll());
		modelAndView.addObject("pessoaObj", new Pessoa());
		
		return modelAndView;
	}
	
	@PostMapping("**/pesquisarpessoa") //post q vem do form do html
	public ModelAndView pesquisar(@RequestParam("nomePesquisa") String nomePesquisa) { //requestparam q vem do post, n na url
		ModelAndView modelAndView=new ModelAndView("cadastro/cadastropessoa");
		
		modelAndView.addObject("pessoas", pessoaRepository.findPessoaByName(nomePesquisa));
		modelAndView.addObject("pessoaObj", new Pessoa());
		
		return modelAndView;
	}
	
	@GetMapping("/telefones/{idpessoa}")
	public ModelAndView telefones(@PathVariable(value = "idpessoa") Long idpessoa) {
		Optional<Pessoa> pessoa=pessoaRepository.findById(idpessoa);
		
		ModelAndView modelAndView=new ModelAndView("cadastro/telefones");
		modelAndView.addObject("pessoaObj", pessoa.get());
		
		Iterable<Pessoa> pessoasIt=pessoaRepository.findAll();
		modelAndView.addObject("pessoas", pessoasIt);
		modelAndView.addObject("telefones", telefoneRepository.getTelefones(idpessoa));
		
		return modelAndView;
	}
	
	@PostMapping("**/addfonepessoa/{idpessoa}")
	public ModelAndView addFonePessoa(Telefone telefone, @PathVariable(value = "idpessoa") Long idpessoa) {
		Pessoa pessoa=pessoaRepository.findById(idpessoa).get();
		
		if(telefone != null && telefone.getNumero().isEmpty() || telefone.getTipo().isEmpty()) {
			
			ModelAndView modelAndView=new ModelAndView("cadastro/telefones");
			modelAndView.addObject("pessoaObj", pessoa);
			modelAndView.addObject("telefones", telefoneRepository.getTelefones(idpessoa));
			
			List<String> msg=new ArrayList<String>();
			if(telefone.getNumero().isEmpty()) {
				msg.add("Número precisa ser informado");
			}
			if(telefone.getTipo().isEmpty()) {
				msg.add("Tipo precisa ser informado");
			}
			modelAndView.addObject("msg", msg);
			
			return modelAndView;
		}
		
		telefone.setPessoa(pessoa);
		
		telefoneRepository.save(telefone);
		
		ModelAndView modelAndView=new ModelAndView("cadastro/telefones");
		modelAndView.addObject("pessoaObj", pessoa);
		modelAndView.addObject("telefones", telefoneRepository.getTelefones(idpessoa));
		
		return modelAndView;
	}
	
	@GetMapping("/removertelefone/{idtelefone}")
	public ModelAndView removerTelefone(@PathVariable(value = "idtelefone") Long idtelefone) {
		Pessoa pessoa=telefoneRepository.findById(idtelefone).get().getPessoa();
		
		telefoneRepository.deleteById(idtelefone);
		
		ModelAndView modelAndView=new ModelAndView("cadastro/telefones");
		modelAndView.addObject("pessoaObj", pessoa);
		modelAndView.addObject("telefones", telefoneRepository.getTelefones(pessoa.getId()));
		
		return modelAndView;
	}
	
}
