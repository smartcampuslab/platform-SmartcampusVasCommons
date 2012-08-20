package eu.trentorise.smartcampus.common;

public class Concept {

	private Long id;
	private String name;
	private String description;
	private String summary;

	public Concept(Long id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public Concept() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}
}
