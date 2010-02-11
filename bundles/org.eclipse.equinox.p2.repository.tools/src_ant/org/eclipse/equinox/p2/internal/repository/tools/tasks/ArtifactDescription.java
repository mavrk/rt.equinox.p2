package org.eclipse.equinox.p2.internal.repository.tools.tasks;

import java.util.Properties;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Parameter;
import org.eclipse.equinox.p2.metadata.*;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.repository.artifact.*;

public class ArtifactDescription extends DataType {

	private String classifier = null;
	private String id = null;
	private String version = null;
	private String range = null;
	private Properties properties = null;

	public void setClassifier(String classifier) {
		this.classifier = classifier;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setRange(String range) {
		this.range = range;
	}

	public void addConfiguredProperty(Parameter property) {
		if (properties == null)
			properties = new Properties();

		properties.put(property.getName(), property.getValue());
	}

	public String getClassifier() {
		return classifier;
	}

	public String getId() {
		return id;
	}

	public String getVersion() {
		return version;
	}

	public IQuery<IArtifactKey> createKeyQuery() {
		VersionRange keyRange = null;
		if (range != null)
			keyRange = new VersionRange(range);
		else if (version != null) {
			Version keyVersion = Version.parseVersion(version);
			keyRange = new VersionRange(keyVersion, true, keyVersion, true);
		}
		return new ArtifactKeyQuery(classifier, id, keyRange);
	}

	public IQuery<IArtifactDescriptor> createDescriptorQuery() {
		VersionRange keyRange = null;
		if (range != null)
			keyRange = new VersionRange(range);
		else if (version != null) {
			Version keyVersion = Version.parseVersion(version);
			keyRange = new VersionRange(keyVersion, true, keyVersion, true);
		}

		ArtifactDescriptorQuery query = new ArtifactDescriptorQuery(id, keyRange, null);
		query.setProperties(properties);
		return query;
	}
}
