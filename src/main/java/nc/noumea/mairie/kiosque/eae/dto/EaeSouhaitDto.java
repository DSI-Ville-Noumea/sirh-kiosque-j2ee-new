package nc.noumea.mairie.kiosque.eae.dto;

public class EaeSouhaitDto {

	private Integer idEaeEvolutionSouhait;
	private String souhait;
	private String suggestion;

	public Integer getIdEaeEvolutionSouhait() {
		return idEaeEvolutionSouhait;
	}

	public void setIdEaeEvolutionSouhait(Integer idEaeEvolutionSouhait) {
		this.idEaeEvolutionSouhait = idEaeEvolutionSouhait;
	}

	public String getSouhait() {
		return souhait;
	}

	public void setSouhait(String souhait) {
		this.souhait = souhait;
	}

	public String getSuggestion() {
		return suggestion;
	}

	public void setSuggestion(String suggestion) {
		this.suggestion = suggestion;
	}
}
