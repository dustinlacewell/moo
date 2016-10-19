/*
 * Copyright (c) 2016, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by Adam <Adam@sigterm.info>
 * 4. Neither the name of the Adam <Adam@sigterm.info> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY Adam <Adam@sigterm.info> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL Adam <Adam@sigterm.info> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.rizon.moo;

import java.io.File;
import java.util.List;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArtifactResolver
{
	private static final Logger logger = LoggerFactory.getLogger(ArtifactResolver.class);
	
	private final File repository;

	public ArtifactResolver()
	{
		repository = new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
	}

	public List<ArtifactResult> resolveArtifacts(Artifact artifact) throws DependencyResolutionException
	{
		RepositorySystem system = newRepositorySystem();

		RepositorySystemSession session = newRepositorySystemSession(system);

		DependencyFilter classpathFlter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE, JavaScopes.RUNTIME);

		CollectRequest collectRequest = new CollectRequest();
		collectRequest.setRoot(new Dependency(artifact, JavaScopes.COMPILE));

		DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, classpathFlter);

		List<ArtifactResult> results = system.resolveDependencies(session, dependencyRequest).getArtifactResults();
		return results;
	}

	private DefaultRepositorySystemSession newRepositorySystemSession(RepositorySystem system)
	{
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

		LocalRepository localRepo = new LocalRepository(repository.getAbsolutePath());
		session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));

		return session;
	}

	private RepositorySystem newRepositorySystem()
	{
		DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();

		return locator.getService(RepositorySystem.class);
	}
}
