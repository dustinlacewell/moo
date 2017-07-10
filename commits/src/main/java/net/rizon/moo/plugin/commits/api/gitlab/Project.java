/*
 * Copyright (c) 2017, Orillion <orillion@rizon.net>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.rizon.moo.plugin.commits.api.gitlab;

/**
 * Represents the Project object in a request body.
 *
 * @author Orillion <orillion@rizon.net>
 */
public class Project
{

	private String name;
	private String description;
	private String web_url;
	private String avatar_url;
	private String git_ssh_url;
	private String git_http_url;
	private String namespace;
	private int visibility_level;
	private String path_with_namespace;
	private String default_branch;

	public String getName()
	{
		return name;
	}

	public String getDescription()
	{
		return description;
	}

	public String getWeb_url()
	{
		return web_url;
	}

	public String getAvatar_url()
	{
		return avatar_url;
	}

	public String getGit_ssh_url()
	{
		return git_ssh_url;
	}

	public String getGit_http_url()
	{
		return git_http_url;
	}

	public String getNamespace()
	{
		return namespace;
	}

	public int getVisibility_level()
	{
		return visibility_level;
	}

	public String getPath_with_namespace()
	{
		return path_with_namespace;
	}

	public String getDefault_branch()
	{
		return default_branch;
	}

}
