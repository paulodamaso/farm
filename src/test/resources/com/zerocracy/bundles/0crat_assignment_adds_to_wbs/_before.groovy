/**
 * Copyright (c) 2016-2017 Zerocracy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to read
 * the Software only. Permissions is hereby NOT GRANTED to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.zerocracy.bundles.assigns_performer

import com.jcabi.github.Event
import com.jcabi.github.Github
import com.jcabi.github.Issue
import com.jcabi.github.Repos
import com.jcabi.xml.XML
import com.zerocracy.jstk.fake.FkFarm
import com.zerocracy.jstk.Project
import com.zerocracy.radars.github.RbOnAssign

import javax.json.Json

def exec(Project project, XML xml) {
  Github github = binding.variables.github
  def repo = github.repos().create(new Repos.RepoCreate("test", false))
  def issue =
      new Issue.Smart(repo.issues().create("hello, world", ""))
  issue.assign("0crat")
  repo.issueEvents()
    .create(Event.ASSIGNED, issue.number(), "yegor256", com.google.common.base.Optional.absent())
  final xpath = String.format(
      "links/link[@rel='github' and @href='%s']",
      repo.coordinates().toString().toLowerCase(Locale.ENGLISH)
  )
  new RbOnAssign().react(
      new FkFarm(project, xpath),
      github,
      Json.createObjectBuilder().add(
          "issue",
          Json.createObjectBuilder().add("number", issue.number())
      ).add(
          "repository",
          Json.createObjectBuilder()
              .add("full_name", repo.coordinates().toString())
      ).add(
          "sender",
          Json.createObjectBuilder().add("login", "yegor256")
      ).build()
  )
}
