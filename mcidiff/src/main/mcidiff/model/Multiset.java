package mcidiff.model;

import java.util.List;

public abstract class Multiset {
	public abstract List<? extends DiffElement> getDiffElements();
}
