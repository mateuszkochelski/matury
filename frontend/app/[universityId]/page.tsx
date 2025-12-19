import { Department } from "../utils/getDepartmentData";
import { getUniversityData } from "../utils/getUniversityData";
import { Breadcrumb } from "@/components/Breadcrumb";
import Map from "@/components/google-map/GoogleMap";
import { Badge } from "@/components/ui/badge/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { BookOpen, ExternalLink, Globe, Mail, MapPin, Phone } from "lucide-react";
import Link from "next/link";
import { notFound } from "next/navigation";

export default async function Page({ params }: { params: Promise<{ universityId: string }> }) {
  const { universityId } = await params;
  if (!universityId) {
    notFound();
  }

  const { universityData, departmentData } = await getUniversityData(universityId);
  const departments = departmentData.content;

  return (
    <>
      <Breadcrumb items={[{ name: universityData.name }]} />
      <div>
        <div className="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-6">
          <div className="flex-1">
            <h1 className="font-bold text-foreground mb-4">{universityData.name}</h1>
            <div className="flex items-center gap-2 text-foreground/70 mb-4">
              <MapPin className="w-5 h-5" />
              <span>{universityData.city}</span>
            </div>
            <p className="text-foreground/80 leading-relaxed">{universityData.description}</p>
          </div>

          <div className="lg:w-80">
            <Card className="border-primary/20">
              <CardHeader>
                <CardTitle className="text-foreground">Kluczowe informacje</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <div className="text-sm text-foreground/60">Studenci</div>
                    <div className="font-semibold text-foreground">TODO</div>
                  </div>
                  <div>
                    <div className="text-sm text-foreground/60">Wydziały</div>
                    <div className="font-semibold text-foreground">{departments.length}</div>
                  </div>
                  <div>
                    <div className="text-sm text-foreground/60">Kierunki</div>
                    <div className="font-semibold text-foreground">TODO</div>
                  </div>
                </div>

                <div className="border-t pt-4 space-y-3">
                  <div className="flex items-center gap-2 text-sm">
                    <Phone className="w-4 h-4 text-foreground/60" />
                    <span className="text-foreground/80">123456789</span>
                  </div>
                  <div className="flex items-center gap-2 text-sm">
                    <Mail className="w-4 h-4 text-foreground/60" />
                    <span className="text-foreground/80">mock@mail.com</span>
                  </div>
                  <div className="flex items-center gap-2 text-sm">
                    <Globe className="w-4 h-4 text-foreground/60" />
                    <a href={universityData.url} className="text-primary hover:underline">
                      {universityData.url}
                    </a>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>

      <Map query={universityData.name}></Map>

      <section>
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-foreground">Wydziały</h2>
          <Badge variant="secondary" className="bg-primary/10 text-primary">
            {departments.length} wydziałów
          </Badge>
        </div>

        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {departments.map((department: Department) => (
            <Card
              key={department.id}
              className="border-primary/20 hover:shadow-lg transition-shadow flex flex-col justify-between"
            >
              <CardHeader>
                <div className="flex items-start justify-between">
                  <div>
                    <CardTitle className="text-foreground text-lg mb-2">
                      {department.name}
                    </CardTitle>
                    <div className="flex items-center gap-4 text-sm text-foreground/60">
                      <div className="flex items-center gap-1">
                        <BookOpen className="w-4 h-4" />
                        <span>? kierunki</span>
                      </div>
                    </div>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <Link href={`/${universityData.id}/${department.id}`}>
                  <Button className="w-full cursor-pointer">
                    Pokaż wydział
                    <ExternalLink className="w-4 h-4 ml-2" />
                  </Button>
                </Link>
              </CardContent>
            </Card>
          ))}
        </div>
      </section>
    </>
  );
}
