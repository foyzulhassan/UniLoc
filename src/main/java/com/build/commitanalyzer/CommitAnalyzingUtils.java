package com.build.commitanalyzer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.gitective.core.BlobUtils;

public class CommitAnalyzingUtils {

	public CommitAnalyzingUtils() {

	}

	public Repository setRepository(String repo_path) throws Exception {
		File gitDir = new File(repo_path);

		RepositoryBuilder builder = new RepositoryBuilder();
		Repository repository;
		repository = builder.setGitDir(gitDir).readEnvironment().findGitDir().build();

		return repository;
	}

	public Iterable<RevCommit> getAllCommits(Git git) throws Exception {
		return git.log().all().call();
	}

	public List<Ref> getAllBranches(Git git) throws Exception {
		return git.branchList().call();
	}

	public File writeContentInFile(String name, String content) throws IOException {
		File f = new File(name);
		
		if (f.exists()) {
			f.delete();
		}
		
		f = new File(name);
		
		content=content.trim();
		if(content.length()<0)
			content="abcdef";
		
		FileUtils.writeStringToFile(f, content);

		return f;
	}

	public String[] getContent(Repository repository, DiffEntry diff, RevCommit commit) {
		return new String[] { BlobUtils.getContent(repository, commit.getId(), diff.getNewPath()),
				BlobUtils.getContent(repository, commit.getParent(0).getId(), diff.getOldPath()) };
	}
	
	public String getContentOnCommit(Repository repository, DiffEntry diff, RevCommit commit) {
		return new String (BlobUtils.getContent(repository, commit.getId(), diff.getNewPath()));
	}

	public DiffFormatter setDiffFormatter(Repository repository, boolean detectRenames) {
		DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);

		df.setRepository(repository);
		df.setDiffComparator(RawTextComparator.DEFAULT);
		df.setDetectRenames(detectRenames);

		return df;
	}
}
