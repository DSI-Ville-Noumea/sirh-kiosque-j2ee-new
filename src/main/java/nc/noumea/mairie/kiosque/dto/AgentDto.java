package nc.noumea.mairie.kiosque.dto;

public class AgentDto {

	private String nom;
	private String prenom;
	private Integer idAgent;
	private String civilite;

	public AgentDto() {

	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getPrenom() {
		return prenom;
	}

	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}

	public Integer getIdAgent() {
		return idAgent;
	}

	public void setIdAgent(Integer idAgent) {
		this.idAgent = idAgent;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		return idAgent.equals(((AgentDto) obj).getIdAgent());
	}

	public String getCivilite() {
		return civilite;
	}

	public void setCivilite(String civilite) {
		this.civilite = civilite;
	}
}