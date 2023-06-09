package curso.springboot.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import curso.springboot.model.Pessoa;
import curso.springboot.model.Telefone;
import curso.springboot.repository.PessoaRepository;
import curso.springboot.repository.ProfissaoRepository;
import curso.springboot.repository.TelefoneRepository;

@Controller
public class PessoaController {

	@Autowired
	private PessoaRepository pessoaRepository;

	@Autowired
	private TelefoneRepository telefoneRepository;
	
	@Autowired
	private ReportUtil reportUtil;
	
	@Autowired
	private ProfissaoRepository profissaoRepository;
	
	//chama as profissoes
	@RequestMapping(method = RequestMethod.GET, value="/cadastropessoa")
	public ModelAndView inicio() {
		ModelAndView modelAndView=new ModelAndView("cadastro/cadastropessoa");
		
		modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		
		modelAndView.addObject("pessoaObj", new Pessoa());
		
		modelAndView.addObject("profissoes", profissaoRepository.findAll());
		
		return modelAndView;
	}
	
	@GetMapping("/pessoaspag")
	public ModelAndView carregaPessoasPorPaginacao(@PageableDefault(size = 5) Pageable pageable, ModelAndView model,
			@RequestParam("nomepesquisa") String nomepesquisa) {
		
		Page<Pessoa> pagePessoa=pessoaRepository.findPessoaByNamePage(nomepesquisa, pageable);
		model.addObject("pessoas", pagePessoa);
		model.addObject("pessoaObj", new Pessoa());
		model.addObject("nomepesquisa", nomepesquisa);
		model.setViewName("cadastro/cadastropessoa");
		
		return model;
	}
	
	@RequestMapping(method = RequestMethod.POST, value="**/salvarpessoa", consumes = {"multipart/form-data"})
	public ModelAndView salvar(@Valid Pessoa pessoa, BindingResult bindingResult, final MultipartFile file) throws IOException {
		pessoa.setTelefones(telefoneRepository.getTelefones(pessoa.getId()));
		
		if(bindingResult.hasErrors()) {
			ModelAndView modelAndView=new ModelAndView("cadastro/cadastropessoa");
			modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
			modelAndView.addObject("pessoaObj", pessoa);
			
			List<String> msg=new ArrayList<String>();
			for (ObjectError objectError : bindingResult.getAllErrors()) {
				msg.add(objectError.getDefaultMessage());
			}
			modelAndView.addObject("msg", msg);
			modelAndView.addObject("profissoes", profissaoRepository.findAll());
			
			return modelAndView;
		}
		
		if(file.getSize() > 0) { //cadastrando novo curriculo
			pessoa.setCurriculo(file.getBytes());
			pessoa.setTipoFileCurriculo(file.getContentType());
			pessoa.setNomeFileCurriculo(file.getOriginalFilename());
		}else {
			if(pessoa.getId() != null && pessoa.getId() > 0) { //editando
				Pessoa pessoaTemp=pessoaRepository.findById(pessoa.getId()).get();
				
				pessoa.setCurriculo(pessoaTemp.getCurriculo());
				pessoa.setTipoFileCurriculo(pessoaTemp.getTipoFileCurriculo());
				pessoa.setNomeFileCurriculo(pessoaTemp.getNomeFileCurriculo());
			}
		}
		
		pessoaRepository.save(pessoa);
		
		ModelAndView modelAndView=new ModelAndView("cadastro/cadastropessoa");
		
		//o thymeleaf carrega todas as pessoas na tabela através desse atributo
		modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		//limpa o form depois de salvar, o thymeleaf pega o atributo
		modelAndView.addObject("pessoaObj", new Pessoa());
		
		return modelAndView;
	}
	
	@RequestMapping(method = RequestMethod.GET, value="/listapessoas")
	public ModelAndView pessoas() {
		ModelAndView modelAndView=new ModelAndView("cadastro/cadastropessoa");
		modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		modelAndView.addObject("pessoaObj", new Pessoa());
		modelAndView.addObject("profissoes", profissaoRepository.findAll());
		
		return modelAndView;
	}
	
	@GetMapping("/editarpessoa/{idpessoa}")
	public ModelAndView editar(@PathVariable(value = "idpessoa") Long idpessoa) {
		Optional<Pessoa> pessoa=pessoaRepository.findById(idpessoa);
		
		ModelAndView modelAndView=new ModelAndView("cadastro/cadastropessoa");
		modelAndView.addObject("pessoaObj", pessoa.get());
		
		modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		modelAndView.addObject("profissoes", profissaoRepository.findAll());
		
		return modelAndView;
	}
	
	@GetMapping("/removerpessoa/{idpessoa}")
	public ModelAndView excluir(@PathVariable(value = "idpessoa") Long idpessoa) {
		pessoaRepository.deleteById(idpessoa);
		
		ModelAndView modelAndView=new ModelAndView("cadastro/cadastropessoa");
		modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		modelAndView.addObject("pessoaObj", new Pessoa());
		modelAndView.addObject("profissoes", profissaoRepository.findAll());
		
		return modelAndView;
	}
	
	@PostMapping("**/pesquisarpessoa") //post q vem do form do html, requestparam q vem do post, n na url
	public ModelAndView pesquisar(@RequestParam("nomepesquisa") String nomepesquisa,
								  @RequestParam("pesquisaSexo") String pesquisaSexo,
								  @PageableDefault(size = 5, sort= {"nome"}) Pageable pageable) {
		
		Page<Pessoa> pessoas=null;
		
		if(pesquisaSexo != null && !pesquisaSexo.isEmpty()) {
			pessoas=pessoaRepository.findPessoaByNameSexoPage(nomepesquisa, pesquisaSexo, pageable);
		}else {
			pessoas=pessoaRepository.findPessoaByNamePage(nomepesquisa, pageable);
		}
		
		ModelAndView modelAndView=new ModelAndView("cadastro/cadastropessoa");
		
		modelAndView.addObject("pessoas", pessoas);
		modelAndView.addObject("pessoaObj", new Pessoa());
		modelAndView.addObject("profissoes", profissaoRepository.findAll());
		modelAndView.addObject("nomepesquisa", nomepesquisa);
		
		return modelAndView;
	}
	
	@GetMapping("**/pesquisarpessoa") //post q vem do form do html, requestparam q vem do post, n na url
	public void imprimePDF(@RequestParam("nomepesquisa") String nomepesquisa, //n retorna nada pra tela, n recarrega, void..
						   @RequestParam("pesquisaSexo") String pesquisaSexo,
						   HttpServletRequest request, HttpServletResponse response) throws Exception {

		List<Pessoa> pessoas=new ArrayList<Pessoa>();
		if(pesquisaSexo != null && !pesquisaSexo.isEmpty() &&
				nomepesquisa != null && !nomepesquisa.isEmpty()) { //busca por nome e sexo
			
			pessoas=pessoaRepository.findPessoaByNameSexo(nomepesquisa, pesquisaSexo);
			
		}else if(nomepesquisa != null && !nomepesquisa.isEmpty()){ //busca so por nome
			
			pessoas=pessoaRepository.findPessoaByName(nomepesquisa);
			
		}else if(pesquisaSexo != null && !pesquisaSexo.isEmpty()) { //busca so por sexo
			
			pessoas=pessoaRepository.findPessoaBySexo(pesquisaSexo);
			
		}else { //busca todos
			Iterable<Pessoa> itPessoas= pessoaRepository.findAll();
			for (Pessoa pessoa : itPessoas) {
				pessoas.add(pessoa);
			}
		}

		//Serviço q faz a geração do relatório
		byte[] pdf=reportUtil.gerarRelatorio(pessoas, "pessoa", request.getServletContext());
		
		//Tamanho da resposta
		response.setContentLength(pdf.length);
		
		//Definir na resposta o tipo de arquivo
		response.setContentType("application/octet-stream");
		
		//Definir o cabeçalho da resposta
		String headerKey="Content-Disposition";
		String headerValue=String.format("attachment; filename=\"%s\"", "relatorio.pdf");
		response.setHeader(headerKey, headerValue);
		
		//Finaliza a resposta para o navegador
		response.getOutputStream().write(pdf);
		
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
	
	@GetMapping("**/baixarcurriculo/{idpessoa}")
	public void baixarcurriculo(@PathVariable(value = "idpessoa") Long idpessoa, HttpServletResponse response) throws IOException {
		
		//Consultar objeto pessoa no banco de dados
		Pessoa pessoa=pessoaRepository.findById(idpessoa).get();
		if(pessoa.getCurriculo() != null) {
			
			//Setar tamanho da resposta
			response.setContentLength(pessoa.getCurriculo().length);
			
			//Tipo do arquivo para download ou pode ser generica usando application/octet-stream
			response.setContentType(pessoa.getTipoFileCurriculo());
			
			//Define o cabeçalho da resposta
			String headerKey="Content-Disposition";
			String headerValue=String.format("attachment; filename=\"%s\"", pessoa.getNomeFileCurriculo());
			response.setHeader(headerKey, headerValue);
			
			//Finaliza a resposta passando o arquivo
			response.getOutputStream().write(pessoa.getCurriculo());
			
		}
		
	}
	
}
