import { notFound } from "next/navigation";

export default async function Page({
  params,
}: {
  params: Promise<{ universityId: string; departmentId: string; fieldId: string }>;
}) {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { universityId, departmentId, fieldId } = await params;

  if (!fieldId) {
    notFound();
  }

  //   // discourage user from providing artificial values
  //   if (fieldData.university.id !== Number(universityId) || fieldData.department.id !== Number(departmentId)) {
  //     return redirect(`/${departmentData.university.id}/${fieldData.department.id}/${fieldId}`)
  //   }

  return <div>"TODO: implement me"</div>;
}
